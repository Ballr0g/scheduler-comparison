package io.scheduler.comparison.quartz.jobs.handlers.pagination

import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.pagination.JobPaginator
import io.scheduler.comparison.quartz.jobs.state.JobState
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata

interface PaginatedJobHandler<T : JobState<*, ChunkedJobMetadata>, V> : JobHandler<T> {

    fun handleNextPage(page: List<V>)

    fun paginator(jobState: T): JobPaginator<T, V>

}
