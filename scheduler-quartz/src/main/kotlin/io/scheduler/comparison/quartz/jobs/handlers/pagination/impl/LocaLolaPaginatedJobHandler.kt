package io.scheduler.comparison.quartz.jobs.handlers.pagination.impl

import io.scheduler.comparison.quartz.domain.OrderRefund
import io.scheduler.comparison.quartz.jobs.JobHandlerNames
import io.scheduler.comparison.quartz.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.quartz.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
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
) : PaginatedJobHandlerBase<DedicatedOrderJobData, DedicatedOrderJobMetadata, OrderRefund>() {

    @Transactional
    override fun executeInternal(orderJobData: DedicatedOrderJobData, orderJobMetadata: DedicatedOrderJobMetadata) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }
        super.executeInternal(orderJobData, orderJobMetadata)
    }

    override fun handleNextPage(page: List<OrderRefund>) {
        locaLolaRefundsSender.sendAllRefunds(page)

        val refundIds = page.asSequence()
            .map { it.id }
            .toSet()
        locaLolaFailuresRepository.closeEligibleForRefunds(refundIds)
    }

    override fun paginator(
        jobData: DedicatedOrderJobData,
        jobMetadata: DedicatedOrderJobMetadata
    ) = listJobPaginator(
        jobData = jobData,
        jobMetadata = jobMetadata,
        pageExtractor = locaLolaFailuresRepository::readAvailableOrderRefunds
    )

}
