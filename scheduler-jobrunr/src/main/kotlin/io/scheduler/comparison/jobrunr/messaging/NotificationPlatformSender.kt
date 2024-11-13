package io.scheduler.comparison.jobrunr.messaging

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.jobrunr.config.properties.KafkaTopics
import io.scheduler.comparison.jobrunr.domain.OperationOnOrder
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class NotificationPlatformSender(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val kafkaTopics: KafkaTopics
) {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    // Todo: throw custom exception if Kafka gives up
    fun sendAllOperationsOnOrder(operations: List<OperationOnOrder>) {
        // Todo: match batch size of actual Kafka batch on the KafkaTemplate

        val targetTopic = kafkaTopics.topics[SupportedKafkaTopics.NOTIFICATION_PLATFORM.value]
            ?: throw IllegalArgumentException("Non-existent topic: ${SupportedKafkaTopics.NOTIFICATION_PLATFORM.value}, "
                    + "available: ${kafkaTopics.topics}")

        CompletableFuture.allOf(*operations.asSequence()
            .map{ kafkaTemplate.send(targetTopic, it) }
            .toList().toTypedArray()
        ).whenComplete { _, throwable ->
            if (throwable == null) {
                log.info { "Successfully sent ${operations.size} operations on order to Notification platform" }
            } else {
                log.warn { "Failed sending operations on order to Notification platform, cause: ${throwable.message}" }
            }
        }
    }

}
