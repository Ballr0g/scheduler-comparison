package io.scheduler.comparison.quartz.jobs

import io.scheduler.comparison.quartz.domain.OrderStatus
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import io.scheduler.comparison.quartz.service.DedicatedJobService
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

/**
 * Dedicated job class instances are used to process only the merchants with specified IDs during initialization.
 */
@Suppress("UNUSED")
class DedicatedOrderJob : Job {

    private lateinit var orderJobData: DedicatedOrderJobData
    private lateinit var orderJobMetadata: DedicatedOrderJobMetadata
    private lateinit var jobService: DedicatedJobService

    override fun execute(context: JobExecutionContext) {
        initJobState(context)
        jobService.executeInternal(orderJobData, orderJobMetadata)
    }

    /**
     * Since Quartz jobs are stateless by design and use reflection (via a parameterless constructor) for instantiation
     * there's no proper way to initialize val properties or directly use Spring DI without customizing
     * SpringBeanJobFactory with AutowireCapableBeanFactory.
     *
     * In this case the necessary values are passed manually without Spring customization via JobExecutionContext,
     * including Spring beans.
     */
    private fun initJobState(context: JobExecutionContext) {
        val jobDataMap = context.jobDetail.jobDataMap
        orderJobData = buildJobData(jobDataMap)
        orderJobMetadata = buildJobMetadata(jobDataMap)

        jobService = jobDataMap[DedicatedOrderJobParams.JOB_HANDLER.value] as? DedicatedJobService
            ?: throw IllegalArgumentException("jobService of invalid type provided")
    }

    private fun buildJobData(jobDataMap: JobDataMap)
        = @Suppress("UNCHECKED_CAST") (DedicatedOrderJobData(
        merchantIds = jobDataMap[DedicatedOrderJobParams.MERCHANT_IDS.value] as? Set<Long>
            ?: throw IllegalArgumentException("merchantIds of invalid types"),
        orderStatuses = jobDataMap[DedicatedOrderJobParams.ORDER_STATUSES.value] as? Set<OrderStatus>
            ?: throw IllegalArgumentException("orderStatuses of invalid types"),
    ))

    private fun buildJobMetadata(jobDataMap: JobDataMap)
        = DedicatedOrderJobMetadata(
        jobName = jobDataMap.getString(DedicatedOrderJobParams.JOB_NAME.value),
        jobCron = jobDataMap.getString(DedicatedOrderJobParams.JOB_CRON.value),
        maxCountPerExecution = jobDataMap.getLong(DedicatedOrderJobParams.MAX_COUNT_PER_EXECUTION.value),
        pageSize = jobDataMap.getLong(DedicatedOrderJobParams.PAGE_SIZE.value),
    )

}
