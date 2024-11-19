package io.scheduler.comparison.jobrunr.jobs.handlers.pagination.impl

import io.scheduler.comparison.jobrunr.domain.OrderRefund
import io.scheduler.comparison.jobrunr.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.jobrunr.jobs.pagination.JobPaginator
import io.scheduler.comparison.jobrunr.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.jobrunr.jobs.requests.pagination.DedicatedLocaLolaJobRequest
import io.scheduler.comparison.jobrunr.jobs.state.impl.DedicatedJobState
import io.scheduler.comparison.jobrunr.service.TransactionalLocaLolaJobService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("pagination")
class LocaLolaPaginatedJobHandler(
    private val transactionalLocaLolaJobService: TransactionalLocaLolaJobService
) : PaginatedJobHandlerBase<DedicatedJobState, OrderRefund, DedicatedLocaLolaJobRequest>() {

    override fun run(jobRequest: DedicatedLocaLolaJobRequest) {
        val jobState = jobRequest.jobState
        log.info { "[${jobState.jobMetadata.jobName}] Started: " +
                "merchantIds=${jobState.jobData.merchantIds}, orderStatuses=${jobState.jobData.orderStatuses}" }

        super.run(jobRequest)
    }

    override fun handleNextPage(paginator: JobPaginator<DedicatedJobState, OrderRefund>)
        = transactionalLocaLolaJobService.processPageTransactionally(paginator)
            .fold<List<OrderRefund>> (
                ifLeft = { ex -> throw ex },
                ifRight = { operations -> return operations }
            )

    override fun paginator(jobState: DedicatedJobState) = listJobPaginator(
        jobState = jobState,
        pageExtractor = transactionalLocaLolaJobService.persistentRefundExtractor()
    )

}
