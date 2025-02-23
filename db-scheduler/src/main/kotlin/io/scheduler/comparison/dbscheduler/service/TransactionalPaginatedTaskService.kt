package io.scheduler.comparison.dbscheduler.service

import arrow.core.Either
import io.scheduler.comparison.dbscheduler.tasks.pagination.TaskPaginator
import io.scheduler.comparison.dbscheduler.tasks.state.TaskState
import io.scheduler.comparison.dbscheduler.tasks.state.data.ChunkedTaskMetadata

interface TransactionalPaginatedTaskService<T : TaskState<*, ChunkedTaskMetadata>, V, E : Throwable> {

    fun processNextPageTransactionally(paginator: TaskPaginator<T, V>): Either<E, List<V>>

    fun persistentPageRefundExtractor(): (pageSize: Int, taskState: T) -> List<V>

}