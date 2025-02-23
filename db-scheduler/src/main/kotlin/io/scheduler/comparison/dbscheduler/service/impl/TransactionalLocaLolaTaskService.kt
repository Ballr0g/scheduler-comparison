package io.scheduler.comparison.dbscheduler.service.impl

import arrow.core.Either
import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.dbscheduler.domain.OrderRefund
import io.scheduler.comparison.dbscheduler.tasks.pagination.TaskPaginator
import io.scheduler.comparison.dbscheduler.tasks.state.impl.DedicatedTaskState
import io.scheduler.comparison.dbscheduler.messaging.LocaLolaRefundsSender
import io.scheduler.comparison.dbscheduler.repositories.pagination.LocaLolaFailuresRepository
import io.scheduler.comparison.dbscheduler.service.TransactionalPaginatedTaskService
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Component
@Profile("pagination")
class TransactionalLocaLolaTaskService(
    private val locaLolaFailuresRepository: LocaLolaFailuresRepository,
    private val locaLolaRefundSender: LocaLolaRefundsSender,
) : TransactionalPaginatedTaskService<DedicatedTaskState, OrderRefund, KafkaException> {

    private companion object {
        private val log = KotlinLogging.logger {}
        const val MAX_KAFKA_WAIT_SECONDS = 5L
    }

    @Transactional
    override fun processNextPageTransactionally(
        paginator: TaskPaginator<DedicatedTaskState, OrderRefund>
    ): Either<KafkaException, List<OrderRefund>> {
        if (!paginator.hasNext()) {
            return Either.Right(emptyList())
        }

        val page = paginator.next()
        val updatedEntries = page.asSequence().map { it.id }.toSet()
        try {
            locaLolaRefundSender.sendOrderRefunds(page)[MAX_KAFKA_WAIT_SECONDS, TimeUnit.SECONDS]
            return Either.Right(locaLolaFailuresRepository.closeEligibleForRefunds(updatedEntries))
        } catch (e: Exception) {
            log.warn { "Task page processing failed with error: ${e.message}" }
            return Either.Left(KafkaException("Task failed with error: ${e.message}", e))
        }
    }

    override fun persistentPageRefundExtractor() = locaLolaFailuresRepository::readAvailableOrderRefunds

}