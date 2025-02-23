package io.scheduler.comparison.dbscheduler.tasks.handlers.pagination.impl

import io.scheduler.comparison.dbscheduler.tasks.TaskHandlerNames
import io.scheduler.comparison.dbscheduler.domain.OrderRefund
import io.scheduler.comparison.dbscheduler.tasks.handlers.pagination.PaginatedTaskHandlerBase
import io.scheduler.comparison.dbscheduler.tasks.state.impl.DedicatedTaskState
import io.scheduler.comparison.dbscheduler.service.TransactionalPaginatedTaskService
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("pagination")
@Component(TaskHandlerNames.LOCA_LOLA_DEDICATED_TASK_HANDLER)
class LocaLolaPaginatedTaskHandler(
    override val transactionalTaskService: TransactionalPaginatedTaskService<DedicatedTaskState, OrderRefund, KafkaException>,
) : PaginatedTaskHandlerBase<DedicatedTaskState, OrderRefund>() {

    override fun run(taskState: DedicatedTaskState) {
        val taskData = taskState.taskData
        log.info { "[${taskState.taskMetadata.taskName}] Started: " +
                "merchantIds=${taskData.merchantIds}, orderStatuses=${taskData.orderStatuses}" }
        super.run(taskState)
    }

}
