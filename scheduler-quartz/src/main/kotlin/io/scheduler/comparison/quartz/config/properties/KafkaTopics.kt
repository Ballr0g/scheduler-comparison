package io.scheduler.comparison.quartz.config.properties

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "kafka")
data class KafkaTopics(
    val topics: Map<@NotBlank String, @NotBlank String> = emptyMap(),
)