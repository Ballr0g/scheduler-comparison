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
        while (pagesLeft > 0 && fetchNextPage(maxPageSize).also { curPageContents = it }.isNotEmpty()) {
            notificationPlatformSender.sendAllOperationsOnOrder(curPageContents)
            --pagesLeft
        }
    }

    fun fetchNextPage(maxPageSize: Long) = operationOnOrderRepository.readUnprocessed(maxPageSize)
}
