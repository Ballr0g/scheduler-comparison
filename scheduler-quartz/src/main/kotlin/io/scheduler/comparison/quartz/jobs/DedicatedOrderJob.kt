package io.scheduler.comparison.quartz.jobs

import io.scheduler.comparison.quartz.domain.OrderStatus
import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Dedicated job class instances are used to process only the merchants with specified IDs during initialization.
 */
@Component
class DedicatedOrderJob : Job {
    @Autowired
    private lateinit var jobHandlers: Map<String, JobHandler<DedicatedOrderJobData, DedicatedOrderJobMetadata>>

    private lateinit var orderJobData: DedicatedOrderJobData
    private lateinit var orderJobMetadata: DedicatedOrderJobMetadata
    private lateinit var jobHandler: JobHandler<DedicatedOrderJobData, DedicatedOrderJobMetadata>

    override fun execute(context: JobExecutionContext) {
        try {
            initJobState(context)
            jobHandler.executeInternal(orderJobData, orderJobMetadata)
        } catch (e: Exception) {
            throw JobExecutionException(e)
        }
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

        val jobHandlerKey = jobDataMap.getString(DedicatedOrderJobParams.JOB_HANDLER.value)
        jobHandler = jobHandlers[jobHandlerKey]
            ?: throw IllegalArgumentException("Unsupported jobHandler=${jobHandlerKey}, " +
                    "available options: ${jobHandlers.keys}")
        orderJobData = buildJobData(jobDataMap)
        orderJobMetadata = buildJobMetadata(jobDataMap)
    }

    private fun buildJobData(jobDataMap: JobDataMap)
        = @Suppress("UNCHECKED_CAST") (DedicatedOrderJobData(
        merchantIds = (jobDataMap[DedicatedOrderJobParams.MERCHANT_IDS.value] as? List<Long>)?.toSet()
            ?: throw IllegalArgumentException("merchantIds of invalid types"),
        orderStatuses = (jobDataMap[DedicatedOrderJobParams.ORDER_STATUSES.value] as? List<OrderStatus>)?.toSet()
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
