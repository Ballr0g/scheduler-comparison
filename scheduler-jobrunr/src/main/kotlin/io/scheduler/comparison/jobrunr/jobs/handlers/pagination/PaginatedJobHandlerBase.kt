package io.scheduler.comparison.jobrunr.jobs.handlers.pagination

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.jobrunr.jobs.requests.StatefulJobRequest
import io.scheduler.comparison.jobrunr.jobs.state.JobState
import io.scheduler.comparison.jobrunr.jobs.state.data.ChunkedJobMetadata

abstract class PaginatedJobHandlerBase<T : JobState<*, ChunkedJobMetadata>, V, K : StatefulJobRequest<T>>
    : PaginatedJobRequestHandler<T, V, K> {

    protected companion object {
        @JvmStatic
        protected val log = KotlinLogging.logger {}
    }

    override fun run(jobRequest: K) {
        val jobState = jobRequest.jobState
        val paginator = paginator(jobState)

        var totalSent = 0
        var pageSize: Int
        while (handleNextPage(paginator).also { pageSize = it.size }.isNotEmpty()) {
            log.info { "Sent operations: [$pageSize/${jobState.jobMetadata.chunkSize}]" }
            totalSent += pageSize
        }

        log.info { "[${jobState.jobMetadata.jobName}] Completed successfully, $totalSent entries handled." }
    }

}
