package io.scheduler.comparison.jobrunr.service.impl

import arrow.core.Either
import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.jobrunr.domain.OperationOnOrder
import io.scheduler.comparison.jobrunr.domain.OrderStatus
import io.scheduler.comparison.jobrunr.jobs.pagination.JobPaginator
import io.scheduler.comparison.jobrunr.jobs.state.impl.DedicatedJobState
import io.scheduler.comparison.jobrunr.messaging.NotificationPlatformSender
import io.scheduler.comparison.jobrunr.repositories.pagination.WildFruitOperationOnOrderRepository
import io.scheduler.comparison.jobrunr.service.TransactionalPaginatedService
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Component
@Profile("pagination")
class TransactionalWildFruitJobService(
    private val operationOnOrderRepository: WildFruitOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) : TransactionalPaginatedService<DedicatedJobState, OperationOnOrder, KafkaException> {

    private companion object {
        private val log = KotlinLogging.logger {}
        const val MAX_KAFKA_WAIT_SECONDS = 5L
    }

    @Transactional
    @Suppress("DuplicatedCode")
    // No common abstraction is expected for Common and WildFruit transactional service classes.
    override fun processNextPageTransactionally(paginator: JobPaginator<DedicatedJobState, OperationOnOrder>): Either<KafkaException, List<OperationOnOrder>> {
        if (!paginator.hasNext()) {
            return Either.Right(emptyList())
        }

        val pageCancellationsExcluded = filteredOutCancellations(paginator.next())
        val updatedIds = pageCancellationsExcluded.asSequence().map { it.id }.toSet()
        try {
            notificationPlatformSender.sendOperationsOnOrder(pageCancellationsExcluded)[MAX_KAFKA_WAIT_SECONDS, TimeUnit.SECONDS]
            return Either.Right(operationOnOrderRepository.updateOrderOperationsOnSuccess(updatedIds))
        } catch (e: Exception) {
            log.warn { "Job failed with error: ${e.message}" }
            operationOnOrderRepository.updateOrderOperationsOnFailure(updatedIds)
            return Either.Left(KafkaException("Job failed with error: ${e.message}", e))
        }
    }

    override fun persistentPageRefundExtractor() = operationOnOrderRepository::readUnprocessedOrders

    private fun filteredOutCancellations(page: List<OperationOnOrder>): List<OperationOnOrder> {
        val cancellations = page.asSequence()
            .filter { it.orderStatus == OrderStatus.CANCELLED }
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsAsError(cancellations)

        return page.asSequence()
            .filter { it.orderStatus != OrderStatus.CANCELLED }
            .toList()
    }

}
