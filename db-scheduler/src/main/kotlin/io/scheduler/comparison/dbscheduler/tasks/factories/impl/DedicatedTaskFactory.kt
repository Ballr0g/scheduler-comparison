package io.scheduler.comparison.dbscheduler.tasks.factories.impl

import com.github.kagkarlsson.scheduler.SchedulerClient
import io.scheduler.comparison.dbscheduler.config.SupportedTaskDescriptors
import io.scheduler.comparison.dbscheduler.tasks.factories.TaskFactoryBase
import io.scheduler.comparison.dbscheduler.tasks.state.impl.DedicatedTaskState
import org.springframework.stereotype.Component

@Component
class DedicatedTaskFactory(
    schedulerClient: SchedulerClient
): TaskFactoryBase<DedicatedTaskState>(schedulerClient) {

    fun scheduleDedicatedLocaLolaTask(dedicatedTaskState: DedicatedTaskState) =
        scheduleTaskByDescriptor(
            dedicatedTaskState,
            SupportedTaskDescriptors.DYNAMIC_RECURRING_DEDICATED_LOCA_LOLA_TASK_DESCRIPTOR
        )

    fun scheduleDedicatedWildFruitTask(dedicatedTaskState: DedicatedTaskState) =
        scheduleTaskByDescriptor(
            dedicatedTaskState,
            SupportedTaskDescriptors.DYNAMIC_RECURRING_DEDICATED_WILD_FRUIT_TASK_DESCRIPTOR
        )

}