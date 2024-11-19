package io.scheduler.comparison.jobrunr.jobs.handlers.pagination.impl

import io.scheduler.comparison.jobrunr.domain.OrderRefund
import io.scheduler.comparison.jobrunr.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.jobrunr.jobs.requests.pagination.DedicatedLocaLolaJobRequest
import io.scheduler.comparison.jobrunr.jobs.state.impl.DedicatedJobState
import io.scheduler.comparison.jobrunr.service.TransactionalPaginatedService
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("pagination")
class LocaLolaPaginatedJobHandler(
    override val transactionalJobService: TransactionalPaginatedService<DedicatedJobState, OrderRefund, KafkaException>
) : PaginatedJobHandlerBase<DedicatedJobState, OrderRefund, DedicatedLocaLolaJobRequest>() {

    override fun run(jobRequest: DedicatedLocaLolaJobRequest) {
        val jobState = jobRequest.jobState
        log.info { "[${jobState.jobMetadata.jobName}] Started: " +
                "merchantIds=${jobState.jobData.merchantIds}, orderStatuses=${jobState.jobData.orderStatuses}" }

        super.run(jobRequest)
    }

}
