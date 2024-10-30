package io.scheduler.comparison.quartz.config

import io.scheduler.comparison.quartz.config.properties.StaticOrderJobProperties
import io.scheduler.comparison.quartz.jobs.CommonOrderJobParams
import io.scheduler.comparison.quartz.jobs.DedicatedOrderJobParams
import io.scheduler.comparison.quartz.service.CommonJobService
import io.scheduler.comparison.quartz.service.DedicatedJobService
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
    val commonJobService: CommonJobService,
    val dedicatedJobService: DedicatedJobService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        jobExecutionProperties.dedicatedMerchantJobs.forEach {
            registerDedicatedOrderHandlingJob(it)
        }

        if (jobExecutionProperties.commonMerchantJobs.isNotEmpty()) {
            val merchantsForExclude = jobExecutionProperties.dedicatedMerchantJobs.asSequence()
                .filter { it.ignoredByCommon }
                .flatMap { it.merchantIds.asSequence() }
                .toSet()

            jobExecutionProperties.commonMerchantJobs.forEach {
                registerCommonOrderHandlingJob(it, merchantsForExclude)
            }
        }
    }

    private fun registerCommonOrderHandlingJob(
        orderJobProperties: StaticOrderJobProperties.StaticCommonOrderJob,
        excludedMerchantIds: Set<Long>
    ) {
        val commonOrderJobDetails = buildCommonOrderHandlingJob(orderJobProperties, excludedMerchantIds)
        val commonOrderTrigger = buildCommonOrderJobTrigger(commonOrderJobDetails, orderJobProperties)

        val scheduler = schedulerFactoryBean.scheduler
        scheduler.scheduleJob(commonOrderJobDetails, commonOrderTrigger)
    }

    private fun buildCommonOrderHandlingJob(
        orderJobProperties: StaticOrderJobProperties.StaticCommonOrderJob,
        excludedMerchantIds: Set<Long>
    ) = JobBuilder.newJob(orderJobProperties.jobClass)
        .withIdentity(orderJobProperties.name)
        .usingJobData(JobDataMap(mapOf(
            CommonOrderJobParams.JOB_NAME.value to orderJobProperties.name,
            CommonOrderJobParams.EXCLUDED_MERCHANT_IDS.value to excludedMerchantIds,
            CommonOrderJobParams.ORDER_STATUSES.value to orderJobProperties.orderStatuses,
            CommonOrderJobParams.JOB_CRON.value to orderJobProperties.cron,
            CommonOrderJobParams.JOB_HANDLER.value to commonJobService,
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
    ) = JobBuilder.newJob(orderJobProperties.jobClass)
        .withIdentity(orderJobProperties.name)
        .usingJobData(JobDataMap(mapOf(
            DedicatedOrderJobParams.JOB_NAME.value to orderJobProperties.name,
            DedicatedOrderJobParams.MERCHANT_IDS.value to orderJobProperties.merchantIds,
            DedicatedOrderJobParams.ORDER_STATUSES.value to orderJobProperties.orderStatuses,
            DedicatedOrderJobParams.JOB_CRON.value to orderJobProperties.cron,
            DedicatedOrderJobParams.JOB_HANDLER.value to dedicatedJobService,
            DedicatedOrderJobParams.PAGE_SIZE.value to orderJobProperties.pageSize,
            DedicatedOrderJobParams.MAX_COUNT_PER_EXECUTION.value to orderJobProperties.maxCountPerExecution,
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