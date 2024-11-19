package io.scheduler.comparison.jobrunr.messaging

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.jobrunr.config.properties.KafkaTopics
import io.scheduler.comparison.jobrunr.domain.OperationOnOrder
import io.scheduler.comparison.jobrunr.domain.OrderRefund
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

    fun sendOperationsOnOrder(operations: Collection<OperationOnOrder>) = send(
        topic = SupportedKafkaTopics.NOTIFICATION_PLATFORM,
        messages = operations,
        onSuccess = { orders -> log.info { "Successfully sent ${orders.size} operations on order to Notification platform" } },
        onError = { throwable -> log.warn { "Failed sending operations on order to Notification platform, cause: ${throwable.message}" } }
    )

    fun sendOrderRefunds(orderRefunds: Collection<OrderRefund>) = send(
        topic = SupportedKafkaTopics.LOCA_LOLA_REFUNDS,
        messages = orderRefunds,
        onSuccess = { messages -> log.info { "Successfully sent ${messages.size} Loca-Lola refunds" } },
        onError = { throwable -> log.warn { "Failed sending Loca-Lola refunds, cause: ${throwable.message}" } },
    )

    private fun <T> send(
        topic: SupportedKafkaTopics,
        messages: Collection<T>,
        onSuccess: ((messages: Collection<T>) -> Unit)? = null,
        onError: ((error: Throwable) -> Unit)? = null,
    ): CompletableFuture<*> {
        val targetTopic = kafkaTopics.topics[topic.value]
            ?: throw IllegalArgumentException("Non-existent topic: ${topic.value}, "
                    + "available: ${kafkaTopics.topics}")

        return CompletableFuture.allOf(*messages.asSequence()
            .map{ kafkaTemplate.send(targetTopic, it) }
            .toList().toTypedArray()
        ).whenComplete { _, throwable ->
            if (throwable == null) {
                onSuccess?.invoke(messages)
            } else {
                onError?.invoke(throwable)
            }
        }
    }

}
