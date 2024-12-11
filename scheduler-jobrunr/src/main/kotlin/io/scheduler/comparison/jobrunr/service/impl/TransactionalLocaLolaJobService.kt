package io.scheduler.comparison.jobrunr.service.impl

import arrow.core.Either
import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.jobrunr.domain.OrderRefund
import io.scheduler.comparison.jobrunr.jobs.pagination.JobPaginator
import io.scheduler.comparison.jobrunr.jobs.state.impl.DedicatedJobState
import io.scheduler.comparison.jobrunr.messaging.LocaLolaRefundsSender
import io.scheduler.comparison.jobrunr.repositories.pagination.LocaLolaFailuresRepository
import io.scheduler.comparison.jobrunr.service.TransactionalPaginatedService
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Component
@Profile("pagination")
class TransactionalLocaLolaJobService(
    private val locaLolaFailuresRepository: LocaLolaFailuresRepository,
    private val locaLolaRefundSender: LocaLolaRefundsSender,
) : TransactionalPaginatedService<DedicatedJobState, OrderRefund, KafkaException> {

    private companion object {
        private val log = KotlinLogging.logger {}
        const val MAX_KAFKA_WAIT_SECONDS = 5L
    }

    @Transactional
    override fun processNextPageTransactionally(
        paginator: JobPaginator<DedicatedJobState, OrderRefund>
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
            log.warn { "Job page processing failed with error: ${e.message}" }
            return Either.Left(KafkaException("Job failed with error: ${e.message}", e))
        }
    }

    override fun persistentPageRefundExtractor() = locaLolaFailuresRepository::readAvailableOrderRefunds

}