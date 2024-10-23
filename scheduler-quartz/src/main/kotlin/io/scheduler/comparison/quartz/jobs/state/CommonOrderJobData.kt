package io.scheduler.comparison.quartz.jobs.state

import io.scheduler.comparison.quartz.domain.OrderStatus

data class CommonOrderJobData(
    val excludedMerchantIds: Set<Long>,
    val orderStatuses: Set<OrderStatus>,
)
