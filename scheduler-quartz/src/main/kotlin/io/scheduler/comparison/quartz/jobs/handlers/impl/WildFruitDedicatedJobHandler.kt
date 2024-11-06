package io.scheduler.comparison.quartz.jobs.handlers.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.domain.OrderStatus
import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import io.scheduler.comparison.quartz.repositories.DedicatedOperationOnOrderRepository
import org.springframework.stereotype.Component

@Component
class WildFruitDedicatedJobHandler(
    private val operationOnOrderRepository: DedicatedOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) : JobHandler<DedicatedOrderJobData, DedicatedOrderJobMetadata> {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun executeInternal(
        orderJobData: DedicatedOrderJobData,
        orderJobMetadata: DedicatedOrderJobMetadata
    ) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }

        val paginator = listJobPaginator(
            jobData = orderJobData,
            jobMetadata = orderJobMetadata,
            pageExtractor = operationOnOrderRepository::readUnprocessedWithReadCountIncrement
        )

        if (!paginator.hasNext()) {
            log.info { "[${orderJobMetadata.jobName}] No new entries available, execution completed" }
            return
        }

        paginator.forEach { handleNextPage(it) }
        log.info { "[${orderJobMetadata.jobName}] Completed successfully" }
    }

    private fun handleNextPage(page: List<OperationOnOrder>) {
        val pageCancellationsExcluded = filteredOutCancellations(page)
        notificationPlatformSender.sendAllOperationsOnOrder(pageCancellationsExcluded)

        val updatedIds = pageCancellationsExcluded.asSequence()
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsAsProcessed(updatedIds)
    }

    private fun filteredOutCancellations(page: List<OperationOnOrder>): List<OperationOnOrder> {
        val cancellations = page.asSequence()
            .filter { it.orderStatus == OrderStatus.CANCELLED }
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsFailed(cancellations)

        return page.asSequence()
            .filter { it.orderStatus != OrderStatus.CANCELLED }
            .toList()
    }


}
