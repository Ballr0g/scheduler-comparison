package io.scheduler.comparison.quartz.jobs

import io.scheduler.comparison.quartz.domain.OrderStatus

data class OrderJobData(
    val merchantIds: List<Long>,
    val orderStatuses: List<OrderStatus>,
)