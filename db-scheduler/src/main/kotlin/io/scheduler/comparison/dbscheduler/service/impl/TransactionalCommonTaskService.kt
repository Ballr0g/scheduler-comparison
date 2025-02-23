package io.scheduler.comparison.dbscheduler.service.impl

import arrow.core.Either
import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.dbscheduler.domain.OperationOnOrder
import io.scheduler.comparison.dbscheduler.tasks.pagination.TaskPaginator
import io.scheduler.comparison.dbscheduler.tasks.state.impl.CommonTaskState
import io.scheduler.comparison.dbscheduler.messaging.NotificationPlatformSender
import io.scheduler.comparison.dbscheduler.repositories.pagination.CommonOperationOnOrderRepository
import io.scheduler.comparison.dbscheduler.service.TransactionalPaginatedTaskService
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Component
@Profile("pagination")
class TransactionalCommonTaskService(
    private val operationOnOrderRepository: CommonOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) : TransactionalPaginatedTaskService<CommonTaskState, OperationOnOrder, KafkaException> {
    private companion object {
        private val log = KotlinLogging.logger {}
        const val MAX_KAFKA_WAIT_SECONDS = 5L
    }

    @Transactional
    @Suppress("DuplicatedCode")
    // No common abstraction is expected for Common and WildFruit transactional service classes.
    override fun processNextPageTransactionally(paginator: TaskPaginator<CommonTaskState, OperationOnOrder>): Either<KafkaException, List<OperationOnOrder>> {
        if (!paginator.hasNext()) {
            return Either.Right(emptyList())
        }

        val page = paginator.next()
        val updatedEntries = page.asSequence().map { it.id }.toSet()
        try {
            notificationPlatformSender.sendOperationsOnOrder(page)[MAX_KAFKA_WAIT_SECONDS, TimeUnit.SECONDS]
            return Either.Right(operationOnOrderRepository.updateOrderOperationsOnSuccess(updatedEntries))
        } catch (e: Exception) {
            log.warn { "Task failed with error: ${e.message}" }
            operationOnOrderRepository.updateOrderOperationsOnFailure(updatedEntries)
            return Either.Left(KafkaException("Task failed with error: ${e.message}", e))
        }
    }

    override fun persistentPageRefundExtractor() = operationOnOrderRepository::readUnprocessedOrders

}