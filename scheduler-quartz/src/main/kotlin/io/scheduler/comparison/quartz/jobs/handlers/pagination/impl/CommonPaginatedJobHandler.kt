package io.scheduler.comparison.quartz.jobs.handlers.pagination.impl

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.JobHandlerNames
import io.scheduler.comparison.quartz.jobs.handlers.pagination.PaginatedJobHandlerBase
import io.scheduler.comparison.quartz.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.quartz.jobs.state.impl.CommonJobState
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
) : PaginatedJobHandlerBase<CommonJobState, OperationOnOrder>() {

    @Transactional
    override fun executeInternal(
        orderJobState: CommonJobState
    ) {
        val jobData = orderJobState.jobData
        log.info { "[${orderJobState.jobMetadata.jobName}] Started: " +
                "excludedMerchantIds=${jobData.excludedMerchantIds}, orderStatuses=${jobData.orderStatuses}" }
        super.executeInternal(orderJobState)
    }

    // Todo: update read_count
    override fun handleNextPage(page: List<OperationOnOrder>) {
        notificationPlatformSender.sendAllOperationsOnOrder(page)
        val updatedIds = page.asSequence()
            .map { it.id }
            .toSet()
        operationOnOrderRepository.markOrderOperationsAsProcessed(updatedIds)
    }

    override fun paginator(jobState: CommonJobState) = listJobPaginator(
        jobState = jobState,
        pageExtractor = operationOnOrderRepository::readUnprocessedWithReadCountIncrement
    )

}
