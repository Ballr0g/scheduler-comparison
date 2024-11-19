package io.scheduler.comparison.quartz.jobs.handlers.pagination.impl

import io.scheduler.comparison.quartz.domain.OrderRefund
import io.scheduler.comparison.quartz.jobs.JobHandlerNames
import io.scheduler.comparison.quartz.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.quartz.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.quartz.jobs.state.impl.DedicatedJobState
import io.scheduler.comparison.quartz.messaging.LocaLolaRefundsSender
import io.scheduler.comparison.quartz.repositories.pagination.LocaLolaFailuresRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Profile("pagination")
@Component(JobHandlerNames.LOCA_LOLA_DEDICATED_JOB_HANDLER)
class LocaLolaPaginatedJobHandler(
    private val locaLolaFailuresRepository: LocaLolaFailuresRepository,
    private val locaLolaRefundsSender: LocaLolaRefundsSender,
) : PaginatedJobHandlerBase<DedicatedJobState, OrderRefund>() {

    @Transactional
    override fun executeInternal(orderJobState: DedicatedJobState) {
        val jobData = orderJobState.jobData
        log.info { "[${orderJobState.jobMetadata.jobName}] Started: " +
                "merchantIds=${jobData.merchantIds}, orderStatuses=${jobData.orderStatuses}" }
        super.executeInternal(orderJobState)
    }

    override fun handleNextPage(page: List<OrderRefund>) {
        locaLolaRefundsSender.sendAllRefunds(page)

        val refundIds = page.asSequence()
            .map { it.id }
            .toSet()
        locaLolaFailuresRepository.closeEligibleForRefunds(refundIds)
    }

    override fun paginator(jobState: DedicatedJobState) = listJobPaginator(
        jobState = jobState,
        pageExtractor = locaLolaFailuresRepository::readAvailableOrderRefunds
    )

}
