package io.scheduler.comparison.quartz.repositories.streaming

import io.scheduler.comparison.quartz.domain.OrderRefund
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import org.intellij.lang.annotations.Language
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*
import java.util.stream.Stream

@Repository
@Profile("streaming")
class LocaLolaStreamingFailuresRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) {

    private companion object {

        @Language("PostgreSQL")
        const val READ_FAILURES_FOR_REFUND_SQL = """
            SELECT orr.id, orr.order_id, orr.merchant_id, orr.eligible_for_refund
            FROM scheduler_quartz.order_refunds orr
            JOIN scheduler_quartz.order_statuses os USING(order_id)
            WHERE
                os.order_status = 'FAILED'
                AND orr.merchant_id IN (:merchantIds)
                AND orr.eligible_for_refund = true
            LIMIT :maxPageSize
            FOR UPDATE OF orr SKIP LOCKED
        """

        @Language("PostgreSQL")
        const val UPDATE_FAILURES_AS_NON_REFUNDABLE_SQL = """
            UPDATE scheduler_quartz.order_refunds
            SET eligible_for_refund = false
            WHERE id IN (:refundIds)
        """

        val orderRefundRowMapper = RowMapper { rs, _ ->
            OrderRefund(
                id = rs.getLong("id"),
                orderId = rs.getObject("order_id", UUID::class.java),
                merchantId = rs.getLong("merchant_id"),
                eligibleForRefund = rs.getBoolean("eligible_for_refund"),
            )
        }

    }

    fun readAvailableOrderRefunds(
        maxPageSize: Int,
        orderJobData: DedicatedOrderJobData
    ): Stream<OrderRefund>
        = jdbcTemplate.queryForStream(READ_FAILURES_FOR_REFUND_SQL,
            mapOf(
                "merchantIds" to orderJobData.merchantIds,
                "maxPageSize" to maxPageSize
            ),
            orderRefundRowMapper
        )

    fun closeEligibleForRefunds(refundIds: Set<Long>)
        = if (refundIds.isNotEmpty()) {
            jdbcTemplate.update(UPDATE_FAILURES_AS_NON_REFUNDABLE_SQL,
                mapOf("refundIds" to refundIds)
            )
        } else 0

}
