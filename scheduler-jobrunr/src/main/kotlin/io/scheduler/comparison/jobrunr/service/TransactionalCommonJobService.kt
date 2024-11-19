package io.scheduler.comparison.jobrunr.service

import arrow.core.Either
import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.jobrunr.domain.OperationOnOrder
import io.scheduler.comparison.jobrunr.jobs.pagination.JobPaginator
import io.scheduler.comparison.jobrunr.jobs.state.impl.CommonJobState
import io.scheduler.comparison.jobrunr.messaging.NotificationPlatformSender
import io.scheduler.comparison.jobrunr.repositories.pagination.CommonOperationOnOrderRepository
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Component
@Profile("pagination")
class TransactionalCommonJobService(
    private val operationOnOrderRepository: CommonOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) {
    private companion object {
        private val log = KotlinLogging.logger {}
        const val MAX_KAFKA_WAIT_SECONDS = 5L
    }

    @Transactional
    fun handleNextPage(paginator: JobPaginator<CommonJobState, OperationOnOrder>): Either<KafkaException, List<OperationOnOrder>> {
        if (!paginator.hasNext()) {
            return Either.Right(emptyList())
        }

        val page = paginator.next()
        val updatedEntries = page.asSequence().map { it.id }.toSet()
        try {
            notificationPlatformSender.sendAllOperationsOnOrder(page)[MAX_KAFKA_WAIT_SECONDS, TimeUnit.SECONDS]
            return Either.Right(operationOnOrderRepository.updateOrderOperationsOnSuccess(updatedEntries))
        } catch (e: Exception) {
            log.warn { "Job failed with error: ${e.message}" }
            operationOnOrderRepository.updateOrderOperationsOnFailure(updatedEntries)
            return Either.Left(KafkaException("Job failed with error: ${e.message}", e))
        }
    }

    fun persistentOrderExtractor() = operationOnOrderRepository::readUnprocessedOrders

}