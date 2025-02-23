package io.scheduler.comparison.dbscheduler.tasks.state.data.impl

import io.scheduler.comparison.dbscheduler.domain.OrderStatus
import java.io.Serializable

data class CommonOrderTaskData(
    // db-scheduler requires a constructor without parameters to properly work with deserialization.
    val excludedMerchantIds: Set<Long> = setOf(),
    val orderStatuses: Set<OrderStatus> = setOf(),
) : Serializable
