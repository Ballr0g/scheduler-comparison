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

    fun sendAllOperationsOnOrder(operations: List<OperationOnOrder>) {
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
