package io.scheduler.comparison.quartz.config.properties

import io.scheduler.comparison.quartz.domain.OrderStatus
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "scheduler.jobs")
data class StaticOrderJobProperties(
    val dedicatedMerchantJobs: List<StaticDedicatedMerchantsOrderJob> = emptyList(),
    val commonMerchantJobs: List<StaticCommonOrderJob> = emptyList(),
){

    data class StaticCommonOrderJob(
        val name: String,
        val orderStatuses: List<OrderStatus>,
        val jobHandler: String = "commonJobHandler",
        val cron: String,
        val pageSize: Long,
        val maxCountPerExecution: Long,
    )

    data class StaticDedicatedMerchantsOrderJob(
        val name: String,
        val merchantIds: List<Long>,
        val orderStatuses: List<OrderStatus>,
        val cron: String,
        val ignoredByCommon: Boolean = true,
        val jobHandler: String = "dedicatedJobHandler",
        val pageSize: Long,
        val maxCountPerExecution: Long,
    )

}