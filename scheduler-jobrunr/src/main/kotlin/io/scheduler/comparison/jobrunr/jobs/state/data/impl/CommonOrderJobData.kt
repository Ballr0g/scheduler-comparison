package io.scheduler.comparison.jobrunr.jobs.state.data.impl

import io.scheduler.comparison.jobrunr.domain.OrderStatus

data class CommonOrderJobData(
    val excludedMerchantIds: Set<Long>,
    val orderStatuses: Set<OrderStatus>,
)
