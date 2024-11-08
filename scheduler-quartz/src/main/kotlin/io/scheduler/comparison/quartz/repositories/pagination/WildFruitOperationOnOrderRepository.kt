package io.scheduler.comparison.quartz.repositories.pagination

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.domain.OrderOperationStatus
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
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
class WildFruitOperationOnOrderRepository(
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

        // Todo: independently execute update after Kafka + limit max page size
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
        orderJobData: DedicatedOrderJobData
    ): List<OperationOnOrder> {
        val updatedOrderStatuses = jdbcClient.sql(READ_UNPROCESSED_ORDER_OPERATIONS_SQL)
            .param("maxPageSize", maxPageSize)
            .param("merchantIds", orderJobData.merchantIds)
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

    fun markOrderOperationsFailed(orderIds: Set<Long>) = markOrderOperationsAs(orderIds, OrderOperationStatus.ERROR)

    fun markOrderOperationsAsProcessed(orderIds: Set<Long>)
        = markOrderOperationsAs(orderIds, OrderOperationStatus.SENT_TO_NOTIFIER)

    fun markOrderOperationsAs(orderIds: Set<Long>, status: OrderOperationStatus)
        = if (orderIds.isNotEmpty()) {
            jdbcClient.sql(CHANGE_ORDER_OPERATION_STATUSES_SQL)
                .param("orderOperationStatus", status, Types.VARCHAR)
                .param("ids", orderIds)
                .param("statusChangeTime", LocalDateTime.now())
                .update()
    } else 0


}
