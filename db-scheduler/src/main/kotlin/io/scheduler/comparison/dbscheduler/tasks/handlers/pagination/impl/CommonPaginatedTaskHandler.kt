package io.scheduler.comparison.dbscheduler.tasks.handlers.pagination.impl

import io.scheduler.comparison.dbscheduler.domain.OperationOnOrder
import io.scheduler.comparison.dbscheduler.tasks.handlers.pagination.PaginatedTaskHandlerBase
import io.scheduler.comparison.dbscheduler.tasks.state.impl.CommonTaskState
import io.scheduler.comparison.dbscheduler.service.TransactionalPaginatedTaskService
import io.scheduler.comparison.dbscheduler.tasks.TaskHandlerNames
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("pagination")
@Component(TaskHandlerNames.COMMON_TASK_HANDLER)
class CommonPaginatedTaskHandler(
    override val transactionalTaskService: TransactionalPaginatedTaskService<CommonTaskState, OperationOnOrder, KafkaException>,
) : PaginatedTaskHandlerBase<CommonTaskState, OperationOnOrder>() {

    override fun run(taskState: CommonTaskState) {
        val taskData = taskState.taskData
        log.info { "[${taskState.taskMetadata.taskName}] Started: " +
                "excludedMerchantIds=${taskData.excludedMerchantIds}, orderStatuses=${taskData.orderStatuses}" }
        super.run(taskState)
    }

}
