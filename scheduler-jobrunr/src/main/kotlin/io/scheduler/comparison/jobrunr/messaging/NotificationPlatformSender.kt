package io.scheduler.comparison.jobrunr.messaging

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.jobrunr.config.properties.KafkaTopics
import io.scheduler.comparison.jobrunr.domain.OperationOnOrder
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class NotificationPlatformSender(
    kafkaTemplate: KafkaTemplate<String, Any>,
    kafkaTopics: KafkaTopics
) : KafkaTemplateSenderBase<OperationOnOrder>(kafkaTemplate, kafkaTopics) {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    fun sendOperationsOnOrder(operations: Collection<OperationOnOrder>) = send(
        topic = SupportedKafkaTopics.NOTIFICATION_PLATFORM,
        messages = operations,
        onSuccess = { orders -> log.info { "Successfully sent ${orders.size} operations on order to Notification platform" } },
        onError = { throwable -> log.warn { "Failed sending operations on order to Notification platform, cause: ${throwable.message}" } }
    )

}
