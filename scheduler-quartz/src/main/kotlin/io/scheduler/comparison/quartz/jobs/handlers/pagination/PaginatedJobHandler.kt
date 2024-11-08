package io.scheduler.comparison.quartz.jobs.handlers.pagination

import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.pagination.JobPaginator
import io.scheduler.comparison.quartz.jobs.state.ChunkedJobMetadata

interface PaginatedJobHandler<T, V : ChunkedJobMetadata, K> : JobHandler<T, V> {

    fun handleNextPage(page: List<K>)

    fun paginator(jobData: T, jobMetadata: V): JobPaginator<T, V, K>

}
