package io.scheduler.comparison.dbscheduler.config

import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerCustomizer
import com.github.kagkarlsson.scheduler.serializer.JacksonSerializer
import com.github.kagkarlsson.scheduler.serializer.Serializer
import com.github.kagkarlsson.scheduler.task.Task
import com.github.kagkarlsson.scheduler.task.TaskDescriptor
import com.github.kagkarlsson.scheduler.task.helper.Tasks
import io.scheduler.comparison.dbscheduler.tasks.TaskHandlerNames
import io.scheduler.comparison.dbscheduler.tasks.handlers.pagination.TaskHandler
import io.scheduler.comparison.dbscheduler.tasks.state.impl.CommonTaskState
import io.scheduler.comparison.dbscheduler.tasks.state.impl.DedicatedTaskState
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

object SupportedTaskDescriptors {
    val DYNAMIC_RECURRING_COMMON_TASK_DESCRIPTOR: TaskDescriptor<CommonTaskState> = TaskDescriptor.of(
        "recurring-common-task",
        CommonTaskState::class.java
    )
    val DYNAMIC_RECURRING_DEDICATED_LOCA_LOLA_TASK_DESCRIPTOR: TaskDescriptor<DedicatedTaskState> = TaskDescriptor.of(
        "recurring-dedicated-loca-lola-task",
        DedicatedTaskState::class.java
    )
    val DYNAMIC_RECURRING_DEDICATED_WILD_FRUIT_TASK_DESCRIPTOR: TaskDescriptor<DedicatedTaskState> = TaskDescriptor.of(
        "recurring-dedicated-wild-fruit-task",
        DedicatedTaskState::class.java
    )
}

@Configuration(proxyBeanMethods = false)
class DbSchedulerConfig(
    private val commonTaskHandlers: Map<String, TaskHandler<CommonTaskState>>,
    private val dedicatedTaskHandlers: Map<String, TaskHandler<DedicatedTaskState>>,
) {

    @Bean
    fun dbSchedulerCustomizer(): DbSchedulerCustomizer {
        return object : DbSchedulerCustomizer {
            override fun serializer(): Optional<Serializer> = Optional.of(JacksonSerializer())
        }
    }

    @Bean
    fun recurringCommonTask(): Task<CommonTaskState> =
        Tasks.recurringWithPersistentSchedule(SupportedTaskDescriptors.DYNAMIC_RECURRING_COMMON_TASK_DESCRIPTOR)
            .execute { taskInstance, _ ->
                val commonTaskHandler = commonTaskHandlers[TaskHandlerNames.COMMON_TASK_HANDLER]
                commonTaskHandler?.run(taskInstance.data)
                    ?: throw IllegalStateException("""
                        Could not find a mandatory handler ${TaskHandlerNames.COMMON_TASK_HANDLER} 
                        for ${SupportedTaskDescriptors.DYNAMIC_RECURRING_COMMON_TASK_DESCRIPTOR}
                    """.trimIndent())
            }

    @Bean
    fun recurringDedicatedLocaLolaTask(): Task<DedicatedTaskState> =
        Tasks.recurringWithPersistentSchedule(SupportedTaskDescriptors.DYNAMIC_RECURRING_DEDICATED_LOCA_LOLA_TASK_DESCRIPTOR)
            .execute { taskInstance, _ ->
                dedicatedTaskHandlers[TaskHandlerNames.LOCA_LOLA_DEDICATED_TASK_HANDLER]?.run(taskInstance.data)
                    ?: throw IllegalStateException("""
                        Could not find a mandatory handler ${TaskHandlerNames.LOCA_LOLA_DEDICATED_TASK_HANDLER} 
                        for ${SupportedTaskDescriptors.DYNAMIC_RECURRING_DEDICATED_LOCA_LOLA_TASK_DESCRIPTOR}
                    """.trimIndent())
            }

    @Bean
    fun recurringDedicatedWildFruitTask(): Task<DedicatedTaskState> =
        Tasks.recurringWithPersistentSchedule(SupportedTaskDescriptors.DYNAMIC_RECURRING_DEDICATED_WILD_FRUIT_TASK_DESCRIPTOR)
            .execute {
                    taskInstance, _ ->
                dedicatedTaskHandlers[TaskHandlerNames.WILD_FRUIT_DEDICATED_TASK_HANDLER]?.run(taskInstance.data)
                    ?: throw IllegalStateException("""
                        Could not find a mandatory handler ${TaskHandlerNames.WILD_FRUIT_DEDICATED_TASK_HANDLER} 
                        for ${SupportedTaskDescriptors.DYNAMIC_RECURRING_DEDICATED_WILD_FRUIT_TASK_DESCRIPTOR}
                    """.trimIndent())
            }

}