package io.scheduler.comparison.quartz.jobs.handlers.streaming.impl

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.domain.OrderStatus
import io.scheduler.comparison.quartz.jobs.JobHandlerNames
import io.scheduler.comparison.quartz.jobs.handlers.streaming.ChunkedStreamJobHandlerBase
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import io.scheduler.comparison.quartz.repositories.streaming.WildFruitStreamingOperationOnOrderRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Profile("streaming")
@Component(JobHandlerNames.WILD_FRUIT_DEDICATED_JOB_HANDLER)
class WildFruitChunkedStreamJobHandler(
    private val operationOnOrderRepository: WildFruitStreamingOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) : ChunkedStreamJobHandlerBase<DedicatedOrderJobData, DedicatedOrderJobMetadata, OperationOnOrder>() {

    @Transactional
    override fun executeInternal(orderJobData: DedicatedOrderJobData, orderJobMetadata: DedicatedOrderJobMetadata) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }
        super.executeInternal(orderJobData, orderJobMetadata)
    }

    override fun openDataStream(
        orderJobData: DedicatedOrderJobData,
        orderJobMetadata: DedicatedOrderJobMetadata
    ) = operationOnOrderRepository.readUnprocessedOperations(orderJobData, orderJobMetadata)

    override fun handleNextChunk(chunk: List<OperationOnOrder>) {
        operationOnOrderRepository.incrementOperationsReadCount(chunk)
        val pageCancellationsExcluded = filteredOutCancellations(chunk)
        notificationPlatformSender.sendAllOperationsOnOrder(pageCancellationsExcluded)

        val updatedIds = pageCancellationsExcluded.asSequence()
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsAsProcessed(updatedIds)
    }

    @Suppress("DuplicatedCode")
    // There are similar handlers for different profiles never intersecting, we're not concerned by duplication.
    private fun filteredOutCancellations(chunk: List<OperationOnOrder>): List<OperationOnOrder> {
        val cancellations = chunk.asSequence()
            .filter { it.orderStatus == OrderStatus.CANCELLED }
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsFailed(cancellations)

        return chunk.asSequence()
            .filter { it.orderStatus != OrderStatus.CANCELLED }
            .toList()
    }

}
