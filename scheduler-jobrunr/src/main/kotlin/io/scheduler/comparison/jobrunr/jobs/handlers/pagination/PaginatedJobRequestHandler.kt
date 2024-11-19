package io.scheduler.comparison.jobrunr.jobs.handlers.pagination

import io.scheduler.comparison.jobrunr.jobs.state.JobState
import io.scheduler.comparison.jobrunr.jobs.pagination.JobPaginator
import org.jobrunr.jobs.lambdas.JobRequest
import org.jobrunr.jobs.lambdas.JobRequestHandler

interface PaginatedJobRequestHandler<T : JobState<*, *>, V, K : JobRequest> : JobRequestHandler<K> {

    fun handleNextPage(paginator: JobPaginator<T, V>): Collection<V>

    fun paginator(jobState: T): JobPaginator<T, V>

}
