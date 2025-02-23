package io.scheduler.comparison.dbscheduler.tasks.state.data.impl

import io.scheduler.comparison.dbscheduler.domain.OrderStatus
import java.io.Serializable

data class DedicatedOrderTaskData(
    // db-scheduler requires a constructor without parameters to properly work with deserialization.
    val merchantIds: Set<Long> = setOf(),
    val orderStatuses: Set<OrderStatus> = setOf(),
) : Serializable
