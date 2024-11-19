package io.scheduler.comparison.jobrunr.jobs.requests.pagination

import io.scheduler.comparison.jobrunr.jobs.handlers.pagination.impl.CommonPaginatedJobHandler
import io.scheduler.comparison.jobrunr.jobs.requests.StatefulJobRequest
import io.scheduler.comparison.jobrunr.jobs.state.data.impl.CommonOrderJobData
import io.scheduler.comparison.jobrunr.jobs.state.data.impl.CommonOrderJobMetadata

import io.scheduler.comparison.jobrunr.jobs.state.impl.CommonJobState

class CommonJobRequest(
    // JobRunr requires a constructor without parameters to properly work with deserialization.
    override val jobState: CommonJobState = CommonJobState(
        jobData = CommonOrderJobData(
            excludedMerchantIds = emptySet(),
            orderStatuses = emptySet(),
        ),
        jobMetadata = CommonOrderJobMetadata(
            jobName = "",
            jobCron = "",
            chunkSize = 0,
            maxCountPerExecution = 0
        )
    )
) : StatefulJobRequest<CommonJobState> {

    override fun getJobRequestHandler() = CommonPaginatedJobHandler::class.java

}
