package io.scheduler.comparison.dbscheduler.tasks.handlers.pagination

import io.scheduler.comparison.dbscheduler.tasks.state.TaskState
import io.scheduler.comparison.dbscheduler.tasks.state.data.TaskMetadata

fun interface TaskHandler<in T : TaskState<*, TaskMetadata>> {

    fun run(taskState: T)

}