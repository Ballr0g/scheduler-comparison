package io.scheduler.comparison.quartz.messaging

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OperationOnOrder
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class NotificationPlatformSender(
    val kafkaTemplate: KafkaTemplate<String, Any>,
    val kafkaProperties: KafkaProperties
) {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    // Todo: throw custom exception if Kafka gives up
    fun sendAllOperationsOnOrder(operations: List<OperationOnOrder>) {
        // Todo: match batch size of actual Kafka batch on the KafkaTemplate
        CompletableFuture.allOf(*operations.asSequence()
            .map{ kafkaTemplate.send(kafkaProperties.template.defaultTopic, it) }
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
