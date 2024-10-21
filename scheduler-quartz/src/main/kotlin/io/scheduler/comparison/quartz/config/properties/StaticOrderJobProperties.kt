package io.scheduler.comparison.quartz.config.properties

import io.scheduler.comparison.quartz.domain.OrderStatus
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "scheduler.jobs")
data class StaticOrderJobProperties(
    val orderJobList: List<StaticOrderJob>
){

    data class StaticOrderJob(
        val name: String,
        val merchantIds: List<Long>,
        val orderStatuses: List<OrderStatus>,
        val cron: String,
    )

}