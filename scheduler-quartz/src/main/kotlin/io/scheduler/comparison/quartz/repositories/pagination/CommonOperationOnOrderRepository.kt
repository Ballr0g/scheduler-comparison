package io.scheduler.comparison.quartz.repositories.pagination

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.domain.OrderOperationStatus
import io.scheduler.comparison.quartz.jobs.state.data.impl.CommonOrderJobData
import org.intellij.lang.annotations.Language
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.sql.Types
import java.time.LocalDateTime

@Repository
@Profile("pagination")
class CommonOperationOnOrderRepository(
    private val jdbcClient: JdbcClient
) {

    private companion object {
        @Language("PostgreSQL")
        const val READ_UNPROCESSED_ORDER_OPERATIONS_FOR_UPDATE_SQL =  """
                SELECT id, order_id, order_statuses.merchant_id, status_change_time,
                    operation_status AS order_operation_status, record_read_count, order_status
                FROM scheduler_quartz.order_statuses
                WHERE
                    operation_status IN ('READY_FOR_PROCESSING', 'FOR_RETRY')
                    -- Support for empty excludedMerchantIds
                    AND NOT (merchant_id = ANY(:excludedMerchantIds))
                    AND order_statuses.order_status IN (:orderStatuses)
                LIMIT :maxPageSize
                FOR UPDATE SKIP LOCKED
        """

        @Language("PostgreSQL")
        const val INCREASE_UNPROCESSED_ORDER_OPERATION_READ_COUNT_SQL = """
            UPDATE scheduler_quartz.order_statuses
            SET record_read_count = record_read_count + 1
            WHERE id IN (:ids)
            RETURNING id, order_id, order_statuses.merchant_id, status_change_time,
                operation_status AS order_operation_status, record_read_count, order_status
        """

        @Language("PostgreSQL")
        const val CHANGE_ORDER_OPERATION_STATUSES_SQL = """
            UPDATE scheduler_quartz.order_statuses
            SET
                operation_status = :orderOperationStatus,
                status_change_time = :statusChangeTime
            WHERE id IN (:ids)
        """
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun readUnprocessedWithReadCountIncrement(
        maxPageSize: Int,
        orderJobData: CommonOrderJobData
    ): List<OperationOnOrder> {
        val updatedOrderStatuses = jdbcClient.sql(READ_UNPROCESSED_ORDER_OPERATIONS_FOR_UPDATE_SQL)
            .param("maxPageSize", maxPageSize)
            .param("excludedMerchantIds", orderJobData.excludedMerchantIds.toTypedArray())
            .param("orderStatuses", orderJobData.orderStatuses.asSequence().map { it.value }.toSet())
            .query(OperationOnOrder::class.java)
            .list()

        // This might happen when the database is either empty or the entries are still locked by another action.
        if (updatedOrderStatuses.isEmpty()) {
            return emptyList()
        }

        return jdbcClient.sql(INCREASE_UNPROCESSED_ORDER_OPERATION_READ_COUNT_SQL)
            .param("ids", updatedOrderStatuses.map { it.id }.toSet())
            .query(OperationOnOrder::class.java)
            .list()
    }

    fun markOrderOperationsAsProcessed(orderIds: Set<Long>)
        = if (orderIds.isNotEmpty()) {
            jdbcClient.sql(CHANGE_ORDER_OPERATION_STATUSES_SQL)
                .param("orderOperationStatus", OrderOperationStatus.SENT_TO_NOTIFIER, Types.VARCHAR)
                .param("statusChangeTime", LocalDateTime.now())
                .param("ids", orderIds)
                .update()
    } else 0
}
