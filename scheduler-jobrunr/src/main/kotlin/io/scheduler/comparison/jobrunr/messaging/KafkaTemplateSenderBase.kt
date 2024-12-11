package io.scheduler.comparison.jobrunr.messaging

import io.scheduler.comparison.jobrunr.config.properties.KafkaTopics
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.CompletableFuture

abstract class KafkaTemplateSenderBase<T>(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val kafkaTopics: KafkaTopics
) {

    protected fun send(
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
