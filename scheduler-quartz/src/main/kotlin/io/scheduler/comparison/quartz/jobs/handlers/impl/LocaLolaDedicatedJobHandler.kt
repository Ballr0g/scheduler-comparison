package io.scheduler.comparison.quartz.jobs.handlers.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OrderRefund
import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.LocaLolaRefundsSender
import io.scheduler.comparison.quartz.repositories.LocaLolaFailuresRepository
import org.springframework.stereotype.Component

@Component
class LocaLolaDedicatedJobHandler(
    private val locaLolaFailuresRepository: LocaLolaFailuresRepository,
    private val locaLolaRefundsSender: LocaLolaRefundsSender,
) : JobHandler<DedicatedOrderJobData, DedicatedOrderJobMetadata> {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun executeInternal(orderJobData: DedicatedOrderJobData, orderJobMetadata: DedicatedOrderJobMetadata) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }

        val paginator = listJobPaginator(
            jobData = orderJobData,
            jobMetadata = orderJobMetadata,
            pageExtractor = locaLolaFailuresRepository::readAvailableOrderRefunds
        )

        if (!paginator.hasNext()) {
            log.info { "[${orderJobMetadata.jobName}] No new entries available, execution completed" }
            return
        }

        paginator.forEach { handleNextPage(it) }
    }

    private fun handleNextPage(page: List<OrderRefund>) {
        locaLolaRefundsSender.sendAllRefunds(page)

        val refundIds = page.asSequence()
            .map { it.id }
            .toSet()
        locaLolaFailuresRepository.closeEligibleForRefunds(refundIds)
    }

}
