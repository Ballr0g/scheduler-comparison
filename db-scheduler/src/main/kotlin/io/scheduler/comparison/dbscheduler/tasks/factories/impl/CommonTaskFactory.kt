package io.scheduler.comparison.dbscheduler.tasks.factories.impl

import com.github.kagkarlsson.scheduler.SchedulerClient
import io.scheduler.comparison.dbscheduler.config.SupportedTaskDescriptors
import io.scheduler.comparison.dbscheduler.tasks.factories.TaskFactoryBase
import io.scheduler.comparison.dbscheduler.tasks.state.impl.CommonTaskState
import org.springframework.stereotype.Component

@Component
class CommonTaskFactory(
    schedulerClient: SchedulerClient
): TaskFactoryBase<CommonTaskState>(schedulerClient) {

    fun scheduleCommonTask(commonTaskState: CommonTaskState) =
        scheduleTaskByDescriptor(
            commonTaskState,
            SupportedTaskDescriptors.DYNAMIC_RECURRING_COMMON_TASK_DESCRIPTOR
        )

}