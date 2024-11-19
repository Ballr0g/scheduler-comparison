package io.scheduler.comparison.jobrunr.jobs.handlers.pagination.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.jobrunr.domain.OperationOnOrder
import io.scheduler.comparison.jobrunr.jobs.handlers.pagination.PaginatedJobRequestHandler
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
) : PaginatedJobRequestHandler<CommonJobState, OperationOnOrder, CommonJobRequest> {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun run(jobRequest: CommonJobRequest) {
        val jobData = jobRequest.jobState.jobData
        val jobMetadata = jobRequest.jobState.jobMetadata
        log.info { "[${jobMetadata.jobName}] Started: " +
                "excludedMerchantIds=${jobData.excludedMerchantIds}, orderStatuses=${jobData.orderStatuses}" }

        val paginator = paginator(jobRequest.jobState)

        var totalSent = 0
        var pageSize: Int
        while (handleNextPage(paginator).also { pageSize = it.size }.isNotEmpty()) {
            log.info { "Sent operations: [$pageSize/${jobMetadata.chunkSize}]" }
            totalSent += pageSize
        }

        log.info { "[${jobMetadata.jobName}] Completed successfully, $totalSent operations sent to Notification Platform." }
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
