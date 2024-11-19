package io.scheduler.comparison.jobrunr.jobs.handlers.pagination.impl

import io.scheduler.comparison.jobrunr.domain.OperationOnOrder
import io.scheduler.comparison.jobrunr.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.jobrunr.jobs.pagination.JobPaginator
import io.scheduler.comparison.jobrunr.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.jobrunr.jobs.requests.pagination.CommonJobRequest
import io.scheduler.comparison.jobrunr.jobs.state.impl.CommonJobState
import io.scheduler.comparison.jobrunr.service.TransactionalCommonJobService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("pagination")
@Component("commonJobHandler")
class CommonPaginatedJobHandler(
    private val transactionalCommonJobService: TransactionalCommonJobService
) : PaginatedJobHandlerBase<CommonJobState, OperationOnOrder, CommonJobRequest>() {

    override fun run(jobRequest: CommonJobRequest) {
        val jobState = jobRequest.jobState
        log.info { "[${jobState.jobMetadata.jobName}] Started: " +
                "excludedMerchantIds=${jobState.jobData.excludedMerchantIds}, orderStatuses=${jobState.jobData.orderStatuses}" }

        super.run(jobRequest)
    }

    override fun handleNextPage(paginator: JobPaginator<CommonJobState, OperationOnOrder>)
        = transactionalCommonJobService.handleNextPage(paginator)
            .fold<List<OperationOnOrder>> (
                ifLeft = { ex -> throw ex },
                ifRight = { operations -> return operations }
            )

    override fun paginator(jobState: CommonJobState) = listJobPaginator(
        jobState = jobState,
        pageExtractor = transactionalCommonJobService.persistentOrderExtractor()
    )

}
