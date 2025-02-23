package io.scheduler.comparison.quartz.jobs.handlers.pagination.impl

import io.scheduler.comparison.quartz.domain.OrderRefund
import io.scheduler.comparison.quartz.jobs.JobHandlerNames
import io.scheduler.comparison.quartz.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.quartz.jobs.state.impl.DedicatedJobState
import io.scheduler.comparison.quartz.service.TransactionalPaginatedService
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("pagination")
@Component(JobHandlerNames.LOCA_LOLA_DEDICATED_JOB_HANDLER)
class LocaLolaPaginatedJobHandler(
    override val transactionalJobService: TransactionalPaginatedService<DedicatedJobState, OrderRefund, KafkaException>,
) : PaginatedJobHandlerBase<DedicatedJobState, OrderRefund>() {

    override fun executeInternal(orderJobState: DedicatedJobState) {
        val jobData = orderJobState.jobData
        log.info { "[${orderJobState.jobMetadata.jobName}] Started: " +
                "merchantIds=${jobData.merchantIds}, orderStatuses=${jobData.orderStatuses}" }
        super.executeInternal(orderJobState)
    }

}
