package io.scheduler.comparison.dbscheduler.tasks.state.impl

import com.github.kagkarlsson.scheduler.task.helper.ScheduleAndData
import com.github.kagkarlsson.scheduler.task.schedule.CronSchedule
import com.github.kagkarlsson.scheduler.task.schedule.Schedule
import io.scheduler.comparison.dbscheduler.tasks.state.TaskState
import io.scheduler.comparison.dbscheduler.tasks.state.data.impl.DedicatedOrderTaskData
import io.scheduler.comparison.dbscheduler.tasks.state.data.impl.DedicatedOrderTaskMetadata

class DedicatedTaskState(
    override val taskData: DedicatedOrderTaskData = DedicatedOrderTaskData(),
    override val taskMetadata: DedicatedOrderTaskMetadata = DedicatedOrderTaskMetadata()
) : TaskState<DedicatedOrderTaskData, DedicatedOrderTaskMetadata>, ScheduleAndData {

    override fun getSchedule(): Schedule = CronSchedule(taskMetadata.taskCron)

    override fun getData(): DedicatedOrderTaskData = taskData

}
