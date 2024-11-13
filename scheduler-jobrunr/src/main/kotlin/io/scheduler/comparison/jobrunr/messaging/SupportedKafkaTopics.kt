package io.scheduler.comparison.jobrunr.messaging

enum class SupportedKafkaTopics(
    val value: String,
) {
    NOTIFICATION_PLATFORM("notification-platform"),
    LOCA_LOLA_REFUNDS("loca-lola-refunds"),
}