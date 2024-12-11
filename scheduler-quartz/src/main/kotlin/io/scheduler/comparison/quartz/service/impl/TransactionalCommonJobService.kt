package io.scheduler.comparison.quartz.service.impl

import arrow.core.Either
import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.pagination.JobPaginator
import io.scheduler.comparison.quartz.jobs.state.impl.CommonJobState
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import io.scheduler.comparison.quartz.repositories.pagination.CommonOperationOnOrderRepository
import io.scheduler.comparison.quartz.service.TransactionalPaginatedService
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
) : TransactionalPaginatedService<CommonJobState, OperationOnOrder, KafkaException> {
    private companion object {
        private val log = KotlinLogging.logger {}
        const val MAX_KAFKA_WAIT_SECONDS = 5L
    }

    @Transactional
    @Suppress("DuplicatedCode")
    // No common abstraction is expected for Common and WildFruit transactional service classes.
    override fun processNextPageTransactionally(paginator: JobPaginator<CommonJobState, OperationOnOrder>): Either<KafkaException, List<OperationOnOrder>> {
        if (!paginator.hasNext()) {
            return Either.Right(emptyList())
        }

        val page = paginator.next()
        val updatedEntries = page.asSequence().map { it.id }.toSet()
        try {
            notificationPlatformSender.sendOperationsOnOrder(page)[MAX_KAFKA_WAIT_SECONDS, TimeUnit.SECONDS]
            return Either.Right(operationOnOrderRepository.updateOrderOperationsOnSuccess(updatedEntries))
        } catch (e: Exception) {
            log.warn { "Job failed with error: ${e.message}" }
            operationOnOrderRepository.updateOrderOperationsOnFailure(updatedEntries)
            return Either.Left(KafkaException("Job failed with error: ${e.message}", e))
        }
    }

    override fun persistentPageRefundExtractor() = operationOnOrderRepository::readUnprocessedOrders

}
