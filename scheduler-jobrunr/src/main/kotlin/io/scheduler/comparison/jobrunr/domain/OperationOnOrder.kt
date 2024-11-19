package io.scheduler.comparison.jobrunr.domain

import java.time.LocalDateTime
import java.util.UUID

data class OperationOnOrder(
    val id: Long,
    val orderId: UUID,
    val merchantId: Long,
    val statusChangeTime: LocalDateTime,
    val orderOperationStatus: OrderOperationStatus,
    val recordReadCount: Long,
    val orderStatus: OrderStatus,
)
