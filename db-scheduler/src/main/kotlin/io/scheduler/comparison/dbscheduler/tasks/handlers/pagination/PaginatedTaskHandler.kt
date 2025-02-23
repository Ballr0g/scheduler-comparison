package io.scheduler.comparison.dbscheduler.tasks.handlers.pagination

import io.scheduler.comparison.dbscheduler.tasks.state.TaskState
import io.scheduler.comparison.dbscheduler.tasks.pagination.TaskPaginator
import io.scheduler.comparison.dbscheduler.tasks.state.data.ChunkedTaskMetadata

interface PaginatedTaskHandler<T : TaskState<*, ChunkedTaskMetadata>, V> : TaskHandler<T> {

    fun handleNextPage(paginator: TaskPaginator<T, V>): Collection<V>

    fun paginator(taskState: T): TaskPaginator<T, V>

}
