package io.scheduler.comparison.quartz.jobs.handlers.streaming

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.LocaLolaRefundsSender
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("streaming")
class LocaLolaStreamingDedicatedJobHandler(
    private val locaLolaRefundsSender: LocaLolaRefundsSender,
) : JobHandler<DedicatedOrderJobData, DedicatedOrderJobMetadata> {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun executeInternal(orderJobData: DedicatedOrderJobData, orderJobMetadata: DedicatedOrderJobMetadata) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }

    }

}
