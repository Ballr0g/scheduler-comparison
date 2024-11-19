package io.scheduler.comparison.quartz.repositories.streaming

import io.scheduler.comparison.quartz.domain.OrderRefund
import io.scheduler.comparison.quartz.jobs.state.impl.DedicatedJobState
import io.scheduler.comparison.quartz.repositories.DomainRowMappers.orderRefundRowMapper
import org.intellij.lang.annotations.Language
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
@Profile("streaming")
class LocaLolaStreamingFailuresRepository(
    private val jdbcOperations: NamedParameterJdbcOperations
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
            FOR UPDATE OF orr SKIP LOCKED
            LIMIT :maxCount
        """

        @Language("PostgreSQL")
        const val UPDATE_FAILURES_AS_NON_REFUNDABLE_SQL = """
            UPDATE scheduler_quartz.order_refunds
            SET eligible_for_refund = false
            WHERE id IN (:refundIds)
        """

    }

    fun readAvailableOrderRefunds(orderJobState: DedicatedJobState): Stream<OrderRefund>
        = jdbcOperations.queryForStream(READ_FAILURES_FOR_REFUND_SQL,
            mapOf(
                "merchantIds" to orderJobState.jobData.merchantIds,
                "maxCount" to orderJobState.jobMetadata.maxCountPerExecution
            ),
            orderRefundRowMapper
        )

    fun closeEligibleForRefunds(refundIds: Set<Long>)
        = if (refundIds.isNotEmpty()) {
            jdbcOperations.update(UPDATE_FAILURES_AS_NON_REFUNDABLE_SQL,
                mapOf("refundIds" to refundIds)
            )
        } else 0

}
