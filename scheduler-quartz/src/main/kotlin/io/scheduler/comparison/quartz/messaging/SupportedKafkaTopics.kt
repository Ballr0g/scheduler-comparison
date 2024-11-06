package io.scheduler.comparison.quartz.messaging

enum class SupportedKafkaTopics(
    val value: String,
) {
    NOTIFICATION_PLATFORM("notification-platform"),
    LOCA_LOLA_REFUNDS("loca-lola-refunds"),
}