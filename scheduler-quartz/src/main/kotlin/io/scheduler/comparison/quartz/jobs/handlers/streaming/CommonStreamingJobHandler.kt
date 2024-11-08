package io.scheduler.comparison.quartz.jobs.handlers.streaming

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import io.scheduler.comparison.quartz.repositories.streaming.CommonStreamingOperationOnOrderRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream
import kotlin.streams.asSequence

@Component("commonJobHandler")
@Profile("streaming")
class CommonStreamingJobHandler(
    private val operationOnOrderRepository: CommonStreamingOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) : JobHandler<CommonOrderJobData, CommonOrderJobMetadata> {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun executeInternal(orderJobData: CommonOrderJobData, orderJobMetadata: CommonOrderJobMetadata) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "excludedMerchantIds=${orderJobData.excludedMerchantIds}, orderStatuses=${orderJobData.orderStatuses}" }

        val availableOperationsStream = operationOnOrderRepository.readUnprocessedOperations(orderJobData, orderJobMetadata)
        consumeDataStream(availableOperationsStream, orderJobMetadata)
        log.info { "[${orderJobMetadata.jobName}] Completed successfully" }
    }

    private fun consumeDataStream(
        operationOnOrderStream: Stream<OperationOnOrder>,
        orderJobMetadata: CommonOrderJobMetadata,
    ) {
        operationOnOrderStream.use {
            it.asSequence()
                .take(orderJobMetadata.maxCountPerExecution.toInt())
                .chunked(orderJobMetadata.pageSize.toInt())
                .forEach { chunk -> handleNextChunk(chunk) }
        }
    }

    private fun handleNextChunk(chunk: List<OperationOnOrder>) {
        val updatedRecords = operationOnOrderRepository.incrementOperationsReadCount(chunk)
        notificationPlatformSender.sendAllOperationsOnOrder(updatedRecords)
        val updatedIds = updatedRecords.asSequence()
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsAsProcessed(updatedIds)
    }

}
