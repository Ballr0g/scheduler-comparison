package io.scheduler.comparison.quartz.repositories

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class OperationOnOrderRepository(
    private val jdbcClient: JdbcClient
) {

    private companion object {
        @Language("PostgreSQL")
        const val READ_UNPROCESSED_ORDER_OPERATIONS_SQL =  """
                SELECT id, order_id, order_statuses.merchant_id, status_change_time,
                    operation_status AS order_operation_status, record_read_count, order_status
                FROM scheduler_quartz.order_statuses
                WHERE
                    operation_status IN ('READY_FOR_PROCESSING', 'FOR_RETRY')
                    AND merchant_id IN (:merchantIds)
                    AND order_statuses.order_status IN (:orderStatuses)
                LIMIT :maxPageSize
                FOR UPDATE SKIP LOCKED
        """

        @Language("PostgreSQL")
        const val INCREASE_UNPROCESSED_ORDER_OPERATION_READ_COUNT_SQL = """
            UPDATE scheduler_quartz.order_statuses
            SET record_read_count = record_read_count + 1
            WHERE id = :id
            RETURNING id, order_id, order_statuses.merchant_id, status_change_time,
                operation_status AS order_operation_status, record_read_count, order_status
        """

        @Language("PostgreSQL")
        const val CHANGE_ORDER_OPERATION_STATUSES_SQL = """
            UPDATE scheduler_quartz.order_statuses
            SET operation_status = :orderOperationStatus
            WHERE id IN (:ids)
        """
    }

    @Transactional
    fun readUnprocessedWithReadCountIncrement(
        maxPageSize: Long,
        orderJobData: DedicatedOrderJobData
    ): List<OperationOnOrder> {
        val updatedOrderStatuses = jdbcClient.sql(READ_UNPROCESSED_ORDER_OPERATIONS_SQL)
            .param("maxPageSize", maxPageSize)
            .param("merchantIds", orderJobData.merchantIds)
            .param("orderStatuses", orderJobData.orderStatuses.asSequence().map { it.value }.toSet())
            .query(OperationOnOrder::class.java)
            .list()

        return updatedOrderStatuses.map {
            jdbcClient.sql(INCREASE_UNPROCESSED_ORDER_OPERATION_READ_COUNT_SQL)
                .param("id", it.id)
                .query(OperationOnOrder::class.java)
                .single()
        }
    }

    fun markOrderOperationsAsProcessed(orderIds: Set<Long>)
        = jdbcClient.sql(CHANGE_ORDER_OPERATION_STATUSES_SQL)
            .param("orderOperationStatus", "SENT_TO_NOTIFIER")
            .param("ids", orderIds)
            .update()
}
