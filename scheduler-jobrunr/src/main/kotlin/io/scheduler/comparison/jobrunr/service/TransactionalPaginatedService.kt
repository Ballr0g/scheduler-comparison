package io.scheduler.comparison.jobrunr.service

import arrow.core.Either
import io.scheduler.comparison.jobrunr.jobs.pagination.JobPaginator
import io.scheduler.comparison.jobrunr.jobs.state.JobState
import io.scheduler.comparison.jobrunr.jobs.state.data.ChunkedJobMetadata

interface TransactionalPaginatedService<T : JobState<*, ChunkedJobMetadata>, V, E : Throwable> {

    fun processNextPageTransactionally(paginator: JobPaginator<T, V>): Either<E, List<V>>

    fun persistentPageRefundExtractor(): (pageSize: Int, jobState: T) -> List<V>

}