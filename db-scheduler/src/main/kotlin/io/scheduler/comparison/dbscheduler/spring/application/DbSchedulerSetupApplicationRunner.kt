package io.scheduler.comparison.dbscheduler.spring.application

import io.scheduler.comparison.dbscheduler.config.properties.StaticOrderJobProperties
import io.scheduler.comparison.dbscheduler.tasks.factories.impl.CommonTaskFactory
import io.scheduler.comparison.dbscheduler.tasks.factories.impl.DedicatedTaskFactory
import io.scheduler.comparison.dbscheduler.tasks.state.data.impl.CommonOrderTaskData
import io.scheduler.comparison.dbscheduler.tasks.state.data.impl.CommonOrderTaskMetadata
import io.scheduler.comparison.dbscheduler.tasks.state.data.impl.DedicatedOrderTaskData
import io.scheduler.comparison.dbscheduler.tasks.state.data.impl.DedicatedOrderTaskMetadata
import io.scheduler.comparison.dbscheduler.tasks.state.impl.CommonTaskState
import io.scheduler.comparison.dbscheduler.tasks.state.impl.DedicatedTaskState
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DbSchedulerSetupApplicationRunner(
    private val jobExecutionProperties: StaticOrderJobProperties,
    private val commonTaskFactory: CommonTaskFactory,
    private val dedicatedTaskFactory: DedicatedTaskFactory,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        jobExecutionProperties.dedicatedMerchantJobs.forEach {
            if (it.jobHandler == "locaLolaDedicatedJobHandler") {
                registerDedicatedLocaLolaOrderJobTask(it)
            }
            else if (it.jobHandler == "wildFruitDedicatedJobHandler") {
                registerDedicatedWildFruitOrderJobTask(it)
            }
        }

        if (jobExecutionProperties.commonMerchantJobs.isNotEmpty()) {
            val merchantsForExclude = jobExecutionProperties.dedicatedMerchantJobs.asSequence()
                .filter { it.ignoredByCommon }
                .flatMap { it.merchantIds.asSequence() }
                .toList()

            jobExecutionProperties.commonMerchantJobs.forEach {
                registerCommonOrderJobTask(it, merchantsForExclude)
            }
        }
    }

    private fun registerCommonOrderJobTask(
        orderJobProperties: StaticOrderJobProperties.StaticCommonOrderJob,
        excludedMerchantIds: List<Long>
    ) = commonTaskFactory.scheduleCommonTask(
        CommonTaskState(
            taskData = CommonOrderTaskData(
                excludedMerchantIds = excludedMerchantIds.toSet(),
                orderStatuses = orderJobProperties.orderStatuses.toSet()
            ),
            taskMetadata = CommonOrderTaskMetadata(
                taskName = orderJobProperties.name,
                taskCron = orderJobProperties.cron,
                chunkSize = orderJobProperties.pageSize,
                maxCountPerExecution = orderJobProperties.maxCountPerExecution,
            )
        )
    )

    private fun registerDedicatedLocaLolaOrderJobTask(
        orderJobProperties: StaticOrderJobProperties.StaticDedicatedMerchantsOrderJob
    ) = dedicatedTaskFactory.scheduleDedicatedLocaLolaTask(
        DedicatedTaskState(
            taskData = DedicatedOrderTaskData(
                merchantIds = orderJobProperties.merchantIds.toSet(),
                orderStatuses = orderJobProperties.orderStatuses.toSet()
            ),
            taskMetadata = DedicatedOrderTaskMetadata(
                taskName = orderJobProperties.name,
                taskCron = orderJobProperties.cron,
                chunkSize = orderJobProperties.pageSize,
                maxCountPerExecution = orderJobProperties.maxCountPerExecution,
            )
        ))

    private fun registerDedicatedWildFruitOrderJobTask(
        orderJobProperties: StaticOrderJobProperties.StaticDedicatedMerchantsOrderJob
    ) = dedicatedTaskFactory.scheduleDedicatedWildFruitTask(
        DedicatedTaskState(
            taskData = DedicatedOrderTaskData(
                merchantIds = orderJobProperties.merchantIds.toSet(),
                orderStatuses = orderJobProperties.orderStatuses.toSet()
            ),
            taskMetadata = DedicatedOrderTaskMetadata(
                taskName = orderJobProperties.name,
                taskCron = orderJobProperties.cron,
                chunkSize = orderJobProperties.pageSize,
                maxCountPerExecution = orderJobProperties.maxCountPerExecution,
            )
        ))

}