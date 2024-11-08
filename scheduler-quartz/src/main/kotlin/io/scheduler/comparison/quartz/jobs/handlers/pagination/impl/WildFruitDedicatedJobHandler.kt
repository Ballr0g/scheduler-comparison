package io.scheduler.comparison.quartz.jobs.handlers.pagination.impl

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.domain.OrderStatus
import io.scheduler.comparison.quartz.jobs.JobHandlerNames
import io.scheduler.comparison.quartz.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.quartz.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import io.scheduler.comparison.quartz.repositories.pagination.WildFruitOperationOnOrderRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Profile("pagination")
@Component(JobHandlerNames.WILD_FRUIT_DEDICATED_JOB_HANDLER)
class WildFruitDedicatedJobHandler(
    private val operationOnOrderRepository: WildFruitOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) : PaginatedJobHandlerBase<DedicatedOrderJobData, DedicatedOrderJobMetadata, OperationOnOrder>() {

    @Transactional
    override fun executeInternal(
        orderJobData: DedicatedOrderJobData,
        orderJobMetadata: DedicatedOrderJobMetadata
    ) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }
        super.executeInternal(orderJobData, orderJobMetadata)
    }

    override fun handleNextPage(page: List<OperationOnOrder>) {
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

    override fun paginator(
        jobData: DedicatedOrderJobData,
        jobMetadata: DedicatedOrderJobMetadata
    ) = listJobPaginator(
        jobData = jobData,
        jobMetadata = jobMetadata,
        pageExtractor = operationOnOrderRepository::readUnprocessedWithReadCountIncrement
    )


}
