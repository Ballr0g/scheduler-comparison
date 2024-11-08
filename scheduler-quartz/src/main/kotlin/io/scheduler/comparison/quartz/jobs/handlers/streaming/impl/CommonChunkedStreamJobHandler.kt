package io.scheduler.comparison.quartz.jobs.handlers.streaming.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.JobHandlerNames
import io.scheduler.comparison.quartz.jobs.handlers.streaming.ChunkedStreamJobHandlerBase
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import io.scheduler.comparison.quartz.repositories.streaming.CommonStreamingOperationOnOrderRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Profile("streaming")
@Component(JobHandlerNames.COMMON_JOB_HANDLER)
class CommonChunkedStreamJobHandler(
    private val operationOnOrderRepository: CommonStreamingOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) : ChunkedStreamJobHandlerBase<CommonOrderJobData, CommonOrderJobMetadata, OperationOnOrder>() {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun executeInternal(orderJobData: CommonOrderJobData, orderJobMetadata: CommonOrderJobMetadata) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "excludedMerchantIds=${orderJobData.excludedMerchantIds}, orderStatuses=${orderJobData.orderStatuses}" }
        super.executeInternal(orderJobData, orderJobMetadata)
    }

    override fun openDataStream(orderJobData: CommonOrderJobData, orderJobMetadata: CommonOrderJobMetadata)
            = operationOnOrderRepository.readUnprocessedOperations(orderJobData, orderJobMetadata)

    override fun handleNextChunk(chunk: List<OperationOnOrder>) {
        val updatedRecords = operationOnOrderRepository.incrementOperationsReadCount(chunk)
        notificationPlatformSender.sendAllOperationsOnOrder(updatedRecords)
        val updatedIds = updatedRecords.asSequence()
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsAsProcessed(updatedIds)
    }

}
