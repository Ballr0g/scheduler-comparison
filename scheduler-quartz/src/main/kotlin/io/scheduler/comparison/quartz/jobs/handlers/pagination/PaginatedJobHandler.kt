package io.scheduler.comparison.quartz.jobs.handlers.pagination

import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.pagination.JobPaginator
import io.scheduler.comparison.quartz.jobs.state.JobState
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata

interface PaginatedJobHandler<T : JobState<*, ChunkedJobMetadata>, V> : JobHandler<T> {

    fun handleNextPage(paginator: JobPaginator<T, V>): Collection<V>

    fun paginator(jobState: T): JobPaginator<T, V>

}
