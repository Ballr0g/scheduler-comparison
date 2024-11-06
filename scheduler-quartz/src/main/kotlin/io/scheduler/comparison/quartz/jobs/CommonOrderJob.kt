package io.scheduler.comparison.quartz.jobs

import io.scheduler.comparison.quartz.domain.OrderStatus
import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobMetadata
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A general-purpose job execution class that works with all merchants except for those used for dedicated jobs.
 */
@Component
class CommonOrderJob : Job {

    private lateinit var orderJobData: CommonOrderJobData
    private lateinit var orderJobMetadata: CommonOrderJobMetadata
    @Transient
    @Autowired
    private lateinit var jobHandler: JobHandler<CommonOrderJobData, CommonOrderJobMetadata>

    override fun execute(context: JobExecutionContext) {
        initJobState(context)
        jobHandler.executeInternal(orderJobData, orderJobMetadata)
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
    }

    private fun buildJobData(jobDataMap: JobDataMap)
            = @Suppress("UNCHECKED_CAST") (CommonOrderJobData(
        excludedMerchantIds = (jobDataMap[CommonOrderJobParams.EXCLUDED_MERCHANT_IDS.value] as? List<Long>)?.toSet()
            ?: throw IllegalArgumentException("merchantIds of invalid types"),
        orderStatuses = (jobDataMap[CommonOrderJobParams.ORDER_STATUSES.value] as? List<OrderStatus>)?.toSet()
            ?: throw IllegalArgumentException("orderStatuses of invalid types"),
    ))

    private fun buildJobMetadata(jobDataMap: JobDataMap)
            = CommonOrderJobMetadata(
        jobName = jobDataMap.getString(CommonOrderJobParams.JOB_NAME.value),
        jobCron = jobDataMap.getString(CommonOrderJobParams.JOB_CRON.value),
        pageSize = jobDataMap.getLong(CommonOrderJobParams.PAGE_SIZE.value),
        maxCountPerExecution = jobDataMap.getLong(CommonOrderJobParams.MAX_COUNT_PER_EXECUTION.value),
    )

}
