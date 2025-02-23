package io.scheduler.comparison.dbscheduler.tasks.state.impl

import com.github.kagkarlsson.scheduler.task.helper.ScheduleAndData
import com.github.kagkarlsson.scheduler.task.schedule.CronSchedule
import com.github.kagkarlsson.scheduler.task.schedule.Schedule
import io.scheduler.comparison.dbscheduler.tasks.state.TaskState
import io.scheduler.comparison.dbscheduler.tasks.state.data.impl.CommonOrderTaskData
import io.scheduler.comparison.dbscheduler.tasks.state.data.impl.CommonOrderTaskMetadata

data class CommonTaskState(
    // db-scheduler requires a constructor without parameters to properly work with deserialization.
    override val taskData: CommonOrderTaskData = CommonOrderTaskData(),
    override val taskMetadata: CommonOrderTaskMetadata = CommonOrderTaskMetadata(),
) : TaskState<CommonOrderTaskData, CommonOrderTaskMetadata>, ScheduleAndData {

    override fun getSchedule(): Schedule = CronSchedule(taskMetadata.taskCron)

    override fun getData(): CommonOrderTaskData = taskData

}
