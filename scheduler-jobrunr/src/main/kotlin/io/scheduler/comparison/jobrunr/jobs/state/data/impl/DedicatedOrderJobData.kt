package io.scheduler.comparison.jobrunr.jobs.state.data.impl

import io.scheduler.comparison.jobrunr.domain.OrderStatus

data class DedicatedOrderJobData(
    val merchantIds: Set<Long>,
    val orderStatuses: Set<OrderStatus>,
)
