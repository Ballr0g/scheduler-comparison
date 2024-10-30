package io.scheduler.comparison.quartz.jobs

import io.scheduler.comparison.quartz.domain.OrderStatus
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobMetadata
import io.scheduler.comparison.quartz.service.CommonJobService
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

/**
 * A general-purpose job execution class that works with all merchants except for those used for dedicated jobs.
 */
@Suppress("UNUSED")
class CommonOrderJob : Job {

    private lateinit var orderJobData: CommonOrderJobData
    private lateinit var orderJobMetadata: CommonOrderJobMetadata
    private lateinit var jobService: CommonJobService

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

        jobService = jobDataMap[CommonOrderJobParams.JOB_HANDLER.value] as? CommonJobService
            ?: throw IllegalArgumentException("jobService of invalid type provided")
    }

    private fun buildJobData(jobDataMap: JobDataMap)
            = @Suppress("UNCHECKED_CAST") (CommonOrderJobData(
        excludedMerchantIds = jobDataMap[CommonOrderJobParams.EXCLUDED_MERCHANT_IDS.value] as? Set<Long>
            ?: throw IllegalArgumentException("merchantIds of invalid types"),
        orderStatuses = jobDataMap[CommonOrderJobParams.ORDER_STATUSES.value] as? Set<OrderStatus>
            ?: throw IllegalArgumentException("orderStatuses of invalid types"),
    ))

    private fun buildJobMetadata(jobDataMap: JobDataMap)
            = CommonOrderJobMetadata(
        jobName = jobDataMap.getString(CommonOrderJobParams.JOB_NAME.value),
        jobCron = jobDataMap.getString(CommonOrderJobParams.JOB_CRON.value),
    )

}
