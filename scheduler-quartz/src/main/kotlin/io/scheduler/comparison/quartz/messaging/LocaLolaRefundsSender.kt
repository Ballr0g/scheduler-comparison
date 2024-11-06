package io.scheduler.comparison.quartz.messaging

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.config.properties.KafkaTopics
import io.scheduler.comparison.quartz.domain.OrderRefund
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class LocaLolaRefundsSender(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val kafkaTopics: KafkaTopics
) {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    // Todo: throw custom exception if Kafka gives up
    fun sendAllRefunds(refunds: List<OrderRefund>) {
        // Todo: match batch size of actual Kafka batch on the KafkaTemplate

        val targetTopic = kafkaTopics.topics[SupportedKafkaTopics.LOCA_LOLA_REFUNDS.value]
            ?: throw IllegalArgumentException("Non-existent topic: ${SupportedKafkaTopics.LOCA_LOLA_REFUNDS.value}, "
                    + "available: ${kafkaTopics.topics}")

        CompletableFuture.allOf(*refunds.asSequence()
            .map{ kafkaTemplate.send(targetTopic, it) }
            .toList().toTypedArray()
        ).whenComplete { _, throwable ->
            if (throwable == null) {
                log.info { "Successfully sent ${refunds.size} Loca-Lola refunds" }
            } else {
                log.warn { "Failed sending Loca-Lola refunds, cause: ${throwable.message}" }
            }
        }
    }

}
