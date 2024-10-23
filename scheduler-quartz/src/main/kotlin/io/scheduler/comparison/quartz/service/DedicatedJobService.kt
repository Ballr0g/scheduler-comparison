package io.scheduler.comparison.quartz.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import org.springframework.stereotype.Component

@Component
class DedicatedJobService {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    fun executeInternal(
        orderJobData: DedicatedOrderJobData,
        orderJobMetadata: DedicatedOrderJobMetadata
    ) {
        log.info { "Job ${orderJobMetadata.jobName}: " +
                "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }
    }

}
