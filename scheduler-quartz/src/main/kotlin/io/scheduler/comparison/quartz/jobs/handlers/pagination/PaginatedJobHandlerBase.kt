package io.scheduler.comparison.quartz.jobs.handlers.pagination

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.jobs.pagination.JobPaginator
import io.scheduler.comparison.quartz.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.quartz.jobs.state.JobState
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata
import io.scheduler.comparison.quartz.service.TransactionalPaginatedService

abstract class PaginatedJobHandlerBase<T : JobState<*, ChunkedJobMetadata>, V> : PaginatedJobHandler<T, V> {

    protected abstract val transactionalJobService: TransactionalPaginatedService<T, V, *>

    protected companion object {
        @JvmStatic
        protected val log = KotlinLogging.logger {}
    }

    override fun executeInternal(orderJobState: T) {
        val paginator = paginator(orderJobState)

        var totalSent = 0
        var pageSize: Int
        while (handleNextPage(paginator).also { pageSize = it.size }.isNotEmpty()) {
            log.info { "[${orderJobState.jobMetadata.jobName}] Sent operations: [$pageSize/${orderJobState.jobMetadata.chunkSize}]" }
            totalSent += pageSize
        }

        log.info { "[${orderJobState.jobMetadata.jobName}] Completed successfully, $totalSent entries handled." }
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
