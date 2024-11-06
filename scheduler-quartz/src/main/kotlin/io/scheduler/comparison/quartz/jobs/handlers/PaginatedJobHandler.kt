package io.scheduler.comparison.quartz.jobs.handlers

import io.scheduler.comparison.quartz.jobs.pagination.JobPaginator
import io.scheduler.comparison.quartz.jobs.state.PaginatedJobMetadata

interface PaginatedJobHandler<T, V : PaginatedJobMetadata, K> : JobHandler<T, V> {

    fun handleNextPage(page: List<K>)

    fun paginator(jobData: T, jobMetadata: V): JobPaginator<T, V, K>

}
