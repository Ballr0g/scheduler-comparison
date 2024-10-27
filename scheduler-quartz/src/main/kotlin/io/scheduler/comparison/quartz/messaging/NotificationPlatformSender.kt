package io.scheduler.comparison.quartz.messaging

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.domain.OperationOnOrder
import org.springframework.stereotype.Component

@Component
class NotificationPlatformSender {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    fun sendAllOperationsOnOrder(operations: List<OperationOnOrder>) {
        // Todo: implement actual sending to Kafka.
        log.info { "Sending all operations on order: $operations" }
    }

}
