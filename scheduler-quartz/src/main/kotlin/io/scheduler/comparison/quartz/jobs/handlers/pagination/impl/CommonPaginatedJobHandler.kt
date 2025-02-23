package io.scheduler.comparison.quartz.jobs.handlers.pagination.impl

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.JobHandlerNames
import io.scheduler.comparison.quartz.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.quartz.jobs.state.impl.CommonJobState
import io.scheduler.comparison.quartz.service.TransactionalPaginatedService
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("pagination")
@Component(JobHandlerNames.COMMON_JOB_HANDLER)
class CommonPaginatedJobHandler(
    override val transactionalJobService: TransactionalPaginatedService<CommonJobState, OperationOnOrder, KafkaException>,
) : PaginatedJobHandlerBase<CommonJobState, OperationOnOrder>() {

    override fun executeInternal(
        orderJobState: CommonJobState
    ) {
        val jobData = orderJobState.jobData
        log.info { "[${orderJobState.jobMetadata.jobName}] Started: " +
                "excludedMerchantIds=${jobData.excludedMerchantIds}, orderStatuses=${jobData.orderStatuses}" }
        super.executeInternal(orderJobState)
    }

}
