package io.scheduler.comparison.dbscheduler.repositories.pagination

import io.scheduler.comparison.dbscheduler.domain.OrderRefund
import io.scheduler.comparison.dbscheduler.tasks.state.impl.DedicatedTaskState
import org.intellij.lang.annotations.Language
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Repository
@Profile("pagination")
class LocaLolaFailuresRepository(
    private val jdbcClient: JdbcClient
) {

    private companion object {

        @Language("PostgreSQL")
        const val READ_FAILURES_FOR_REFUND_SQL = """
            SELECT orr.id, orr.order_id, orr.merchant_id, orr.eligible_for_refund
            FROM db_scheduler.order_refunds orr
            JOIN db_scheduler.order_statuses os USING(order_id)
            WHERE
                os.order_status = 'FAILED'
                AND orr.merchant_id IN (:merchantIds)
                AND orr.eligible_for_refund = true
            LIMIT :maxPageSize
            FOR UPDATE OF orr SKIP LOCKED
        """

        @Language("PostgreSQL")
        const val UPDATE_FAILURES_AS_NON_REFUNDABLE_SQL = """
            UPDATE db_scheduler.order_refunds
            SET eligible_for_refund = false
            WHERE id IN (:refundIds)
            RETURNING id, order_id, merchant_id, eligible_for_refund
        """

    }

    @Transactional(propagation = Propagation.MANDATORY)
    fun readAvailableOrderRefunds(
        maxPageSize: Int,
        orderTaskState: DedicatedTaskState
    ): List<OrderRefund>
        = jdbcClient.sql(READ_FAILURES_FOR_REFUND_SQL)
        .param("merchantIds", orderTaskState.taskData.merchantIds)
        .param("maxPageSize", maxPageSize)
        .query(OrderRefund::class.java)
        .list()

    @Transactional(propagation = Propagation.MANDATORY)
    fun closeEligibleForRefunds(refundIds: Collection<Long>)
            = if (refundIds.isNotEmpty()) {
        jdbcClient.sql(UPDATE_FAILURES_AS_NON_REFUNDABLE_SQL)
            .param("refundIds", refundIds)
            .query(OrderRefund::class.java)
            .list()
    } else emptyList()

}
