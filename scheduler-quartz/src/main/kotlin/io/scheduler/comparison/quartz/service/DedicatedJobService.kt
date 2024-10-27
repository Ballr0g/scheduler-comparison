package io.scheduler.comparison.quartz.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.repositories.OperationOnOrderRepository
import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

@Component
class DedicatedJobService(
    private val operationOnOrderRepository: OperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender
) {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    fun executeInternal(
        orderJobData: DedicatedOrderJobData,
        orderJobMetadata: DedicatedOrderJobMetadata
    ) {
        log.info { "Job ${orderJobMetadata.jobName}: " +
                "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }

        val maxPageSize = orderJobMetadata.pageSize
        var pagesLeft = ceil(orderJobMetadata.maxCountPerExecution.toDouble() / maxPageSize).toLong()
        var curPageContents: List<OperationOnOrder> = emptyList()
        while (
            pagesLeft-- > 0
            && fetchNextPage(maxPageSize, orderJobData).also { curPageContents = it }.isNotEmpty()
        ) {
            notificationPlatformSender.sendAllOperationsOnOrder(curPageContents)
            val updatedIds = curPageContents.asSequence()
                .map { it.id }
                .toSet()
            operationOnOrderRepository.markOrderOperationsAsProcessed(updatedIds)
        }
    }

    fun fetchNextPage(maxPageSize: Long, orderJobData: DedicatedOrderJobData)
        = operationOnOrderRepository.readUnprocessedWithReadCountIncrement(maxPageSize, orderJobData)
}
