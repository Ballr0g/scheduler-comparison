package io.scheduler.comparison.jobrunr.domain

enum class OrderStatus(
    val value: String
) {
    PAID("PAID"),
    DELIVERED("DELIVERED"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");
}