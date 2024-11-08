package io.scheduler.comparison.quartz.repositories

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.domain.OrderOperationStatus
import io.scheduler.comparison.quartz.domain.OrderRefund
import io.scheduler.comparison.quartz.domain.OrderStatus
import org.springframework.jdbc.core.RowMapper
import java.util.*

object DomainRowMappers {

    val operationOnOrderRowMapper = RowMapper { rs, _ ->
        OperationOnOrder(
            id = rs.getLong("id"),
            orderId = rs.getObject("order_id", UUID::class.java),
            merchantId = rs.getLong("merchant_id"),
            statusChangeTime = rs.getTimestamp("status_change_time").toLocalDateTime(),
            orderOperationStatus = OrderOperationStatus.valueOf(rs.getString("order_operation_status")),
            recordReadCount = rs.getLong("record_read_count"),
            orderStatus = OrderStatus.valueOf(rs.getString("order_status")),
        )
    }

    val orderRefundRowMapper = RowMapper { rs, _ ->
        OrderRefund(
            id = rs.getLong("id"),
            orderId = rs.getObject("order_id", UUID::class.java),
            merchantId = rs.getLong("merchant_id"),
            eligibleForRefund = rs.getBoolean("eligible_for_refund"),
        )
    }

}