package io.scheduler.comparison.quartz.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kafka")
data class KafkaTopics(
    val topics: Map<String, String> = emptyMap(),
)