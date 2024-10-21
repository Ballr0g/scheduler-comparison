package io.scheduler.comparison.quartz.config

import io.scheduler.comparison.quartz.config.properties.StaticOrderJobProperties
import io.scheduler.comparison.quartz.jobs.OrderHandlingJob
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.TriggerBuilder
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.stereotype.Component

@Component
class QuartzSetupApplicationRunner(
    val jobExecutionProperties: StaticOrderJobProperties,
    val schedulerFactoryBean: SchedulerFactoryBean
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        for (job in jobExecutionProperties.orderJobList) {
            registerOrderHandlingJob(job)
        }
    }

    private fun registerOrderHandlingJob(orderJobProperties: StaticOrderJobProperties.StaticOrderJob) {
        val orderHandlingJobDetails = buildJobDetails(orderJobProperties)
        val orderHandlingTrigger = buildJobTrigger(orderHandlingJobDetails, orderJobProperties)

        val scheduler = schedulerFactoryBean.scheduler
        scheduler.scheduleJob(orderHandlingJobDetails, orderHandlingTrigger)

    }

    private fun buildJobDetails(orderJobProperties: StaticOrderJobProperties.StaticOrderJob)
        = JobBuilder.newJob(OrderHandlingJob::class.java)
        .withIdentity(orderJobProperties.name)
        .usingJobData(JobDataMap(mapOf(
            "name" to orderJobProperties.name,
            "merchantIds" to orderJobProperties.merchantIds,
            "orderStatuses" to orderJobProperties.orderStatuses,
            "cron" to orderJobProperties.cron,
        )))
        .build()

    private fun buildJobTrigger(
        orderHandlingJobDetails: JobDetail,
        orderJobProperties: StaticOrderJobProperties.StaticOrderJob
    ) = TriggerBuilder.newTrigger()
        .forJob(orderHandlingJobDetails)
        .withIdentity("${orderJobProperties.name} trigger")
        .withSchedule(CronScheduleBuilder
            .cronSchedule(orderJobProperties.cron)
            .withMisfireHandlingInstructionDoNothing()
        )
        .build()
}