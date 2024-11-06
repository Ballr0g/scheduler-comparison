package io.scheduler.comparison.quartz.jobs.handlers.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.handlers.PaginatedJobHandlerBase
import io.scheduler.comparison.quartz.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import io.scheduler.comparison.quartz.repositories.CommonOperationOnOrderRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("pagination")
class CommonJobHandler(
    private val operationOnOrderRepository: CommonOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) : PaginatedJobHandlerBase<CommonOrderJobData, CommonOrderJobMetadata, OperationOnOrder>() {

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
       super.executeInternal(orderJobData, orderJobMetadata)
    }

    override fun handleNextPage(page: List<OperationOnOrder>) {
        notificationPlatformSender.sendAllOperationsOnOrder(page)
        val updatedIds = page.asSequence()
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsAsProcessed(updatedIds)
    }

    override fun paginator(
        jobData: CommonOrderJobData,
        jobMetadata: CommonOrderJobMetadata
    ) = listJobPaginator(
        jobData = jobData,
        jobMetadata = jobMetadata,
        pageExtractor = operationOnOrderRepository::readUnprocessedWithReadCountIncrement
    )

}
