package io.scheduler.comparison.quartz.jobs.handlers.pagination.impl

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.JobHandlerNames
import io.scheduler.comparison.quartz.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.quartz.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.quartz.jobs.state.data.impl.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.data.impl.CommonOrderJobMetadata
import io.scheduler.comparison.quartz.messaging.NotificationPlatformSender
import io.scheduler.comparison.quartz.repositories.pagination.CommonOperationOnOrderRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Profile("pagination")
@Component(JobHandlerNames.COMMON_JOB_HANDLER)
class CommonPaginatedJobHandler(
    private val operationOnOrderRepository: CommonOperationOnOrderRepository,
    private val notificationPlatformSender: NotificationPlatformSender,
) : PaginatedJobHandlerBase<CommonOrderJobData, CommonOrderJobMetadata, OperationOnOrder>() {

    @Transactional
    override fun executeInternal(
        orderJobData: CommonOrderJobData,
        orderJobMetadata: CommonOrderJobMetadata
    ) {
        log.info { "[${orderJobMetadata.jobName}] Started: " +
                "excludedMerchantIds=${orderJobData.excludedMerchantIds}, orderStatuses=${orderJobData.orderStatuses}" }
       super.executeInternal(orderJobData, orderJobMetadata)
    }

    // Todo: update read_count
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
