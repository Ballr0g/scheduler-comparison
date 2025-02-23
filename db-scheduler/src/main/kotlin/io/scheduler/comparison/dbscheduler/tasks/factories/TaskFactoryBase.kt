package io.scheduler.comparison.dbscheduler.tasks.factories

import com.github.kagkarlsson.scheduler.SchedulerClient
import com.github.kagkarlsson.scheduler.task.TaskDescriptor
import io.scheduler.comparison.dbscheduler.tasks.state.TaskState
import io.scheduler.comparison.dbscheduler.tasks.state.data.TaskMetadata

open class TaskFactoryBase<T : TaskState<*, TaskMetadata>>(
    private val schedulerClient: SchedulerClient
) {

    protected fun scheduleTaskByDescriptor(
        taskState: T,
        taskDescriptor: TaskDescriptor<T>,
    ) = schedulerClient.scheduleIfNotExists(taskDescriptor.instance(taskState.taskMetadata.taskName)
        .data(taskState).scheduledAccordingToData()
    )

}