package io.scheduler.comparison.quartz.spring.application

import io.scheduler.comparison.quartz.config.properties.StaticOrderJobProperties
import io.scheduler.comparison.quartz.jobs.CommonOrderJob
import io.scheduler.comparison.quartz.jobs.CommonOrderJobParams
import io.scheduler.comparison.quartz.jobs.DedicatedOrderJob
import io.scheduler.comparison.quartz.jobs.DedicatedOrderJobParams
import io.scheduler.comparison.quartz.jobs.state.data.impl.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.data.impl.CommonOrderJobMetadata
import io.scheduler.comparison.quartz.jobs.state.data.impl.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.data.impl.DedicatedOrderJobMetadata
import io.scheduler.comparison.quartz.jobs.state.impl.CommonJobState
import io.scheduler.comparison.quartz.jobs.state.impl.DedicatedJobState
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
    val schedulerFactoryBean: SchedulerFactoryBean,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        jobExecutionProperties.dedicatedMerchantJobs.forEach {
            registerDedicatedOrderHandlingJob(it)
        }

        if (jobExecutionProperties.commonMerchantJobs.isNotEmpty()) {
            val merchantsForExclude = jobExecutionProperties.dedicatedMerchantJobs.asSequence()
                .filter { it.ignoredByCommon }
                .flatMap { it.merchantIds.asSequence() }
                .toList()

            jobExecutionProperties.commonMerchantJobs.forEach {
                registerCommonOrderHandlingJob(it, merchantsForExclude)
            }
        }
    }

    private fun registerCommonOrderHandlingJob(
        orderJobProperties: StaticOrderJobProperties.StaticCommonOrderJob,
        excludedMerchantIds: List<Long>
    ) {
        val commonOrderJobDetails = buildCommonOrderHandlingJob(orderJobProperties, excludedMerchantIds)
        val commonOrderTrigger = buildCommonOrderJobTrigger(commonOrderJobDetails, orderJobProperties)

        val scheduler = schedulerFactoryBean.scheduler
        scheduler.scheduleJob(commonOrderJobDetails, commonOrderTrigger)
    }

    private fun buildCommonOrderHandlingJob(
        orderJobProperties: StaticOrderJobProperties.StaticCommonOrderJob,
        excludedMerchantIds: List<Long>
    ) = JobBuilder.newJob(CommonOrderJob::class.java)
        .withIdentity(orderJobProperties.name)
        .usingJobData(JobDataMap(mapOf(
            CommonOrderJobParams.JOB_STATE.value to CommonJobState(
                jobData = CommonOrderJobData(
                    excludedMerchantIds = excludedMerchantIds.toSet(),
                    orderStatuses = orderJobProperties.orderStatuses.toSet()
                ),
                jobMetadata = CommonOrderJobMetadata(
                    jobName = orderJobProperties.name,
                    jobCron = orderJobProperties.cron,
                    chunkSize = orderJobProperties.pageSize,
                    maxCountPerExecution = orderJobProperties.maxCountPerExecution,
                )
            ),
            CommonOrderJobParams.JOB_HANDLER.value to orderJobProperties.jobHandler,
        )))
        .build()

    private fun buildCommonOrderJobTrigger(
        orderHandlingJobDetails: JobDetail,
        orderJobProperties: StaticOrderJobProperties.StaticCommonOrderJob
    ) = TriggerBuilder.newTrigger()
        .forJob(orderHandlingJobDetails)
        .withIdentity("${orderJobProperties.name} trigger")
        .withSchedule(CronScheduleBuilder
            .cronSchedule(orderJobProperties.cron)
            .withMisfireHandlingInstructionDoNothing()
        )
        .build()

    private fun registerDedicatedOrderHandlingJob(
        orderJobProperties: StaticOrderJobProperties.StaticDedicatedMerchantsOrderJob
    ) {
        val dedicatedOrderJobDetails = buildDedicatedOrderJobDetails(orderJobProperties)
        val dedicatedOrderTrigger = buildDedicatedOrderJobTrigger(dedicatedOrderJobDetails, orderJobProperties)

        val scheduler = schedulerFactoryBean.scheduler
        scheduler.scheduleJob(dedicatedOrderJobDetails, dedicatedOrderTrigger)
    }

    private fun buildDedicatedOrderJobDetails(
        orderJobProperties: StaticOrderJobProperties.StaticDedicatedMerchantsOrderJob
    ) = JobBuilder.newJob(DedicatedOrderJob::class.java)
        .withIdentity(orderJobProperties.name)
        .usingJobData(JobDataMap(mapOf(
            DedicatedOrderJobParams.JOB_STATE.value to DedicatedJobState(
                jobData = DedicatedOrderJobData(
                    merchantIds = orderJobProperties.merchantIds.toSet(),
                    orderStatuses = orderJobProperties.orderStatuses.toSet()
                ),
                jobMetadata = DedicatedOrderJobMetadata(
                    jobName = orderJobProperties.name,
                    jobCron = orderJobProperties.cron,
                    chunkSize = orderJobProperties.pageSize,
                    maxCountPerExecution = orderJobProperties.maxCountPerExecution,
                )
            ),
            DedicatedOrderJobParams.JOB_HANDLER.value to orderJobProperties.jobHandler,
        )))
        .build()

    private fun buildDedicatedOrderJobTrigger(
        orderHandlingJobDetails: JobDetail,
        orderJobProperties: StaticOrderJobProperties.StaticDedicatedMerchantsOrderJob
    ) = TriggerBuilder.newTrigger()
        .forJob(orderHandlingJobDetails)
        .withIdentity("${orderJobProperties.name} trigger")
        .withSchedule(CronScheduleBuilder
            .cronSchedule(orderJobProperties.cron)
            .withMisfireHandlingInstructionDoNothing()
        )
        .build()
}