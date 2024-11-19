package io.scheduler.comparison.jobrunr.spring.application

import io.scheduler.comparison.jobrunr.config.properties.StaticOrderJobProperties
import io.scheduler.comparison.jobrunr.jobs.requests.pagination.CommonJobRequest
import io.scheduler.comparison.jobrunr.jobs.state.data.impl.CommonOrderJobData
import io.scheduler.comparison.jobrunr.jobs.state.data.impl.CommonOrderJobMetadata
import io.scheduler.comparison.jobrunr.jobs.state.impl.CommonJobState
import org.jobrunr.scheduling.JobRequestScheduler
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class JobRunrSetupApplicationRunner(
    private val jobExecutionProperties: StaticOrderJobProperties,
    private val jobScheduler: JobRequestScheduler,
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
        jobScheduler.scheduleRecurrently(orderJobProperties.cron, CommonJobRequest(CommonJobState(
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
        )))
    }

    private fun registerDedicatedOrderHandlingJob(
        orderJobProperties: StaticOrderJobProperties.StaticDedicatedMerchantsOrderJob
    ) {
        TODO("Create a JobRunr-specific dedicated implementation")
    }

}