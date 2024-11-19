package io.scheduler.comparison.quartz.jobs.handlers.streaming.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OrderRefund
import io.scheduler.comparison.quartz.jobs.JobHandlerNames
import io.scheduler.comparison.quartz.jobs.handlers.streaming.ChunkedStreamJobHandlerBase
import io.scheduler.comparison.quartz.jobs.state.data.impl.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.data.impl.DedicatedOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.LocaLolaRefundsSender
import io.scheduler.comparison.quartz.repositories.streaming.LocaLolaStreamingFailuresRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Profile("streaming")
@Component(JobHandlerNames.LOCA_LOLA_DEDICATED_JOB_HANDLER)
class LocaLolaChunkedStreamJobHandler(
    private val locaLolaFailuresRepository: LocaLolaStreamingFailuresRepository,
    private val locaLolaRefundsSender: LocaLolaRefundsSender,
) : ChunkedStreamJobHandlerBase<DedicatedOrderJobData, DedicatedOrderJobMetadata, OrderRefund>() {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun executeInternal(orderJobData: DedicatedOrderJobData, orderJobMetadata: DedicatedOrderJobMetadata) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }
        super.executeInternal(orderJobData, orderJobMetadata)
    }


    override fun openDataStream(orderJobData: DedicatedOrderJobData, orderJobMetadata: DedicatedOrderJobMetadata
    ) = locaLolaFailuresRepository.readAvailableOrderRefunds(
        orderJobData = orderJobData,
        orderJobMetadata = orderJobMetadata,
    )

    override fun handleNextChunk(chunk: List<OrderRefund>) {
        locaLolaRefundsSender.sendAllRefunds(chunk)

        val refundIds = chunk.asSequence()
            .map { it.id }
            .toSet()
        locaLolaFailuresRepository.closeEligibleForRefunds(refundIds)
    }

}
