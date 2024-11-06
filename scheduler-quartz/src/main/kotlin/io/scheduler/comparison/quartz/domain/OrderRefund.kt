package io.scheduler.comparison.quartz.domain

import java.util.UUID

data class OrderRefund(
    val id: Long,
    val orderId: UUID,
    val merchantId: Long,
    val eligibleForRefund: Boolean,
)