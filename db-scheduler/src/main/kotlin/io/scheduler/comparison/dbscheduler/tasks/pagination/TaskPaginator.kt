package io.scheduler.comparison.dbscheduler.tasks.pagination

import io.scheduler.comparison.dbscheduler.tasks.state.TaskState
import io.scheduler.comparison.dbscheduler.tasks.state.data.ChunkedTaskMetadata

interface TaskPaginator<T : TaskState<*, ChunkedTaskMetadata>, V> : Iterator<List<V>> {

    val taskState: T
    val pageSize: Int

}
