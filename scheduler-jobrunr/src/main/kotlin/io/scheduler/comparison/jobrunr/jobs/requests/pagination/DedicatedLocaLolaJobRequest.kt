package io.scheduler.comparison.jobrunr.jobs.requests.pagination

import io.scheduler.comparison.jobrunr.jobs.handlers.pagination.impl.LocaLolaPaginatedJobHandler
import io.scheduler.comparison.jobrunr.jobs.requests.StatefulJobRequest
import io.scheduler.comparison.jobrunr.jobs.state.data.impl.DedicatedOrderJobData
import io.scheduler.comparison.jobrunr.jobs.state.data.impl.DedicatedOrderJobMetadata
import io.scheduler.comparison.jobrunr.jobs.state.impl.DedicatedJobState

class DedicatedLocaLolaJobRequest(
    // JobRunr requires a constructor without parameters to properly work with deserialization.
    override val jobState: DedicatedJobState = DedicatedJobState(
        jobData = DedicatedOrderJobData(
            merchantIds = emptySet(),
            orderStatuses = emptySet(),
        ),
        jobMetadata = DedicatedOrderJobMetadata(
            jobName = "",
            jobCron = "",
            chunkSize = 0,
            maxCountPerExecution = 0
        )
    ),
) : StatefulJobRequest<DedicatedJobState> {

    override fun getJobRequestHandler() = LocaLolaPaginatedJobHandler::class.java

}
