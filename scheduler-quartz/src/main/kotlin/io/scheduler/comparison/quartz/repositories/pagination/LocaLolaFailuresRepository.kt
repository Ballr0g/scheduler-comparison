package io.scheduler.comparison.quartz.repositories.pagination

import io.scheduler.comparison.quartz.domain.OrderRefund
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import org.intellij.lang.annotations.Language
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
@Profile("pagination")
class LocaLolaFailuresRepository(
    private val jdbcClient: JdbcClient
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

    }

    fun readAvailableOrderRefunds(
        maxPageSize: Long,
        orderJobData: DedicatedOrderJobData
    ): List<OrderRefund>
        = jdbcClient.sql(READ_FAILURES_FOR_REFUND_SQL)
        .param("merchantIds", orderJobData.merchantIds)
        .param("maxPageSize", maxPageSize)
        .query(OrderRefund::class.java)
        .list()

    fun closeEligibleForRefunds(refundIds: Set<Long>)
            = if (refundIds.isNotEmpty()) {
        jdbcClient.sql(UPDATE_FAILURES_AS_NON_REFUNDABLE_SQL)
            .param("refundIds", refundIds)
            .update()
    } else 0

}
