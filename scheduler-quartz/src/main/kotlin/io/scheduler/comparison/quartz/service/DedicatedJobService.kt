package io.scheduler.comparison.quartz.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.repositories.OperationOnOrderRepository
import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.pagination.DedicatedJobPaginator
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

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

        retrievePages(orderJobData, orderJobMetadata).forEach { handleNextPage(it) }
    }

    private fun handleNextPage(page: List<OperationOnOrder>) {
        notificationPlatformSender.sendAllOperationsOnOrder(page)
        val updatedIds = page.asSequence()
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsAsProcessed(updatedIds)
    }

    private fun retrievePages(orderJobData: DedicatedOrderJobData,
                              orderJobMetadata: DedicatedOrderJobMetadata) = DedicatedJobPaginator(
        jobData = orderJobData,
        jobMetadata = orderJobMetadata,
        pageExtractor = operationOnOrderRepository::readUnprocessedWithReadCountIncrement
    )

}
