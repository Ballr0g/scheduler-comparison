package io.scheduler.comparison.jobrunr.jobs.handlers.pagination.impl

import io.scheduler.comparison.jobrunr.domain.OperationOnOrder
import io.scheduler.comparison.jobrunr.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.jobrunr.jobs.requests.pagination.CommonJobRequest
import io.scheduler.comparison.jobrunr.jobs.state.impl.CommonJobState
import io.scheduler.comparison.jobrunr.service.TransactionalPaginatedService
import org.apache.kafka.common.KafkaException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("pagination")
@Component("commonJobHandler")
class CommonPaginatedJobHandler(
    override val transactionalJobService: TransactionalPaginatedService<CommonJobState, OperationOnOrder, KafkaException>
) : PaginatedJobHandlerBase<CommonJobState, OperationOnOrder, CommonJobRequest>() {

    override fun run(jobRequest: CommonJobRequest) {
        val jobState = jobRequest.jobState
        log.info { "[${jobState.jobMetadata.jobName}] Started: " +
                "excludedMerchantIds=${jobState.jobData.excludedMerchantIds}, orderStatuses=${jobState.jobData.orderStatuses}" }

        super.run(jobRequest)
    }

}
