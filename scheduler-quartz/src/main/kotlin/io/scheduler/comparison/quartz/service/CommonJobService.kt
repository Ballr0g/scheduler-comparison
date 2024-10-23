package io.scheduler.comparison.quartz.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobMetadata
import org.springframework.stereotype.Component

@Component
class CommonJobService {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    fun executeInternal(
        orderJobData: CommonOrderJobData,
        orderJobMetadata: CommonOrderJobMetadata
    ) {
        log.info { "Job ${orderJobMetadata.jobName}: " +
                "excludedMerchantIds=${orderJobData.excludedMerchantIds}, orderStatuses=${orderJobData.orderStatuses}" }
    }

}
