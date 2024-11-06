package io.scheduler.comparison.quartz.jobs.handlers.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import org.springframework.stereotype.Component

@Component
class DedicatedJobHandler : JobHandler<DedicatedOrderJobData, DedicatedOrderJobMetadata> {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun executeInternal(
        orderJobData: DedicatedOrderJobData,
        orderJobMetadata: DedicatedOrderJobMetadata
    ) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }
    }

}
