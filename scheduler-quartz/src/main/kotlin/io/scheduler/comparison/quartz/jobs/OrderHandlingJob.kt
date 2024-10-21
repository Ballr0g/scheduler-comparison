package io.scheduler.comparison.quartz.jobs

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OrderStatus
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

class OrderHandlingJob : Job {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    private lateinit var orderJobData: OrderJobData
    private lateinit var orderJobMetadata: OrderJobMetadata

    override fun execute(context: JobExecutionContext) {
        val jobDataMap = context.jobDetail.jobDataMap
        orderJobData = buildJobData(jobDataMap)
        orderJobMetadata = buildJobMetadata(jobDataMap)

        log.info { "merchantIds=${orderJobData.merchantIds}, orderStatuses=${orderJobData.orderStatuses}" }
    }

    private fun buildJobData(jobDataMap: JobDataMap)
        = @Suppress("UNCHECKED_CAST") OrderJobData(
        merchantIds = jobDataMap["merchantIds"] as? List<Long>
            ?: throw IllegalArgumentException("merchantIds of invalid types"),
        orderStatuses = jobDataMap["orderStatuses"] as? List<OrderStatus>
            ?: throw IllegalArgumentException("orderStatuses of invalid types"),
    )

    private fun buildJobMetadata(jobDataMap: JobDataMap)
        = OrderJobMetadata(
        jobName = jobDataMap.getString("name"),
        jobCron = jobDataMap.getString("cron"),
    )

}