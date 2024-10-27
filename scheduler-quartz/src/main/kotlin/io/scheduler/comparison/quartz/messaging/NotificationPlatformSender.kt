package io.scheduler.comparison.quartz.messaging

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import org.springframework.stereotype.Component

@Component
class NotificationPlatformSender {

    fun sendAllOperationsOnOrder(operations: List<OperationOnOrder>) {
        TODO("Implement sending to notification platform Kafka")
    }

}
