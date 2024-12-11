package io.scheduler.comparison.quartz.service

import arrow.core.Either
import io.scheduler.comparison.quartz.jobs.pagination.JobPaginator
import io.scheduler.comparison.quartz.jobs.state.JobState
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata

interface TransactionalPaginatedService<T : JobState<*, ChunkedJobMetadata>, V, E : Throwable> {

    fun processNextPageTransactionally(paginator: JobPaginator<T, V>): Either<E, List<V>>

    fun persistentPageRefundExtractor(): (pageSize: Int, jobState: T) -> List<V>

}