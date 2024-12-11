package io.scheduler.comparison.jobrunr.messaging

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.jobrunr.config.properties.KafkaTopics
import io.scheduler.comparison.jobrunr.domain.OrderRefund
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class LocaLolaRefundsSender(
    kafkaTemplate: KafkaTemplate<String, Any>,
    kafkaTopics: KafkaTopics
) : KafkaTemplateSenderBase<OrderRefund>(kafkaTemplate, kafkaTopics) {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    fun sendOrderRefunds(orderRefunds: Collection<OrderRefund>) = send(
        topic = SupportedKafkaTopics.LOCA_LOLA_REFUNDS,
        messages = orderRefunds,
        onSuccess = { messages -> log.info { "Successfully sent ${messages.size} Loca-Lola refunds" } },
        onError = { throwable -> log.warn { "Failed sending Loca-Lola refunds, cause: ${throwable.message}" } },
    )

}
