package io.scheduler.comparison.quartz.jobs.handlers.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.pagination.impl.CommonJobPaginator
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import io.scheduler.comparison.quartz.repositories.CommonOperationOnOrderRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CommonJobHandler(
    private val operationOnOrderRepository: CommonOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) : JobHandler<CommonOrderJobData, CommonOrderJobMetadata> {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun executeInternal(
        orderJobData: CommonOrderJobData,
        orderJobMetadata: CommonOrderJobMetadata
    ) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "excludedMerchantIds=${orderJobData.excludedMerchantIds}, orderStatuses=${orderJobData.orderStatuses}" }

        val paginator = createPaginator(orderJobData, orderJobMetadata)
        if (!paginator.hasNext()) {
            log.info { "[${orderJobMetadata.jobName}] No new entries available, execution completed" }
            return
        }

        paginator.forEach { handleNextPage(it) }
        log.info { "[${orderJobMetadata.jobName}] Completed successfully" }
    }

    private fun handleNextPage(page: List<OperationOnOrder>) {
        notificationPlatformSender.sendAllOperationsOnOrder(page)
        val updatedIds = page.asSequence()
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsAsProcessed(updatedIds)
    }

    private fun createPaginator(orderJobData: CommonOrderJobData,
                                orderJobMetadata: CommonOrderJobMetadata
    ) = CommonJobPaginator(
        jobData = orderJobData,
        jobMetadata = orderJobMetadata,
        pageExtractor = operationOnOrderRepository::readUnprocessedWithReadCountIncrement
    )

}
