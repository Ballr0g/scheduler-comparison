package io.scheduler.comparison.quartz.domain

enum class OrderOperationStatus {
    READY_FOR_PROCESSING,
    SENT_TO_NOTIFIER,
    FOR_RETRY,
    RETRIES_EXCEEDED,
    ERROR
}