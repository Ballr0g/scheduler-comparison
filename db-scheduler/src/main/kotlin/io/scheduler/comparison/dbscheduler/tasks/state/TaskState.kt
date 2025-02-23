package io.scheduler.comparison.dbscheduler.tasks.state

import io.scheduler.comparison.dbscheduler.tasks.state.data.TaskMetadata
import java.io.Serializable

interface TaskState<out T, out V : TaskMetadata> : Serializable {

    val taskData: T
    val taskMetadata: V

}
