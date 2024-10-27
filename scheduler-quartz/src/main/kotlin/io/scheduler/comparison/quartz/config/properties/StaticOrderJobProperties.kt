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
        val orderStatuses: Set<OrderStatus>,
        val cron: String,
    )

    data class StaticDedicatedMerchantsOrderJob(
        val name: String,
        val merchantIds: Set<Long>,
        val orderStatuses: Set<OrderStatus>,
        val cron: String,
        val ignoredByCommon: Boolean = true,
        val pageSize: Long = -1,
        val maxCountPerExecution: Long = -1
    )

}