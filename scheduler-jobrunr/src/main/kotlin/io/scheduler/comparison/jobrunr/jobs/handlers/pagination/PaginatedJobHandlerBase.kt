package io.scheduler.comparison.jobrunr.jobs.handlers.pagination

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.jobrunr.jobs.pagination.JobPaginator
import io.scheduler.comparison.jobrunr.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.jobrunr.jobs.requests.StatefulJobRequest
import io.scheduler.comparison.jobrunr.jobs.state.JobState
import io.scheduler.comparison.jobrunr.jobs.state.data.ChunkedJobMetadata
import io.scheduler.comparison.jobrunr.service.TransactionalPaginatedService

abstract class PaginatedJobHandlerBase<T : JobState<*, ChunkedJobMetadata>, V, K : StatefulJobRequest<T>> : PaginatedJobRequestHandler<T, V, K> {

    protected abstract val transactionalJobService: TransactionalPaginatedService<T, V, *>

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
            log.info { "[${jobState.jobMetadata.jobName}] Sent operations: [$pageSize/${jobState.jobMetadata.chunkSize}]" }
            totalSent += pageSize
        }

        log.info { "[${jobState.jobMetadata.jobName}] Completed successfully, $totalSent entries handled." }
    }

    override fun handleNextPage(paginator: JobPaginator<T, V>)
        = transactionalJobService.processNextPageTransactionally(paginator)
            .fold<List<V>> (
                ifLeft = { ex -> throw ex },
                ifRight = { operations -> return operations }
            )

    override fun paginator(jobState: T) = listJobPaginator(
        jobState = jobState,
        pageExtractor = transactionalJobService.persistentPageRefundExtractor()
    )

}
