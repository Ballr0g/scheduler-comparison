package io.scheduler.comparison.jobrunr.config.spring.application

import io.scheduler.comparison.jobrunr.config.properties.StaticOrderJobProperties
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class JobRunrSetupApplicationRunner(
    val jobExecutionProperties: StaticOrderJobProperties,
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
        TODO("Create a JobRunr-specific common implementation")
    }

    private fun registerDedicatedOrderHandlingJob(
        orderJobProperties: StaticOrderJobProperties.StaticDedicatedMerchantsOrderJob
    ) {
        TODO("Create a JobRunr-specific dedicated implementation")
    }

}