package io.scheduler.comparison.jobrunr.repositories.pagination

import io.scheduler.comparison.jobrunr.domain.OperationOnOrder
import io.scheduler.comparison.jobrunr.domain.OrderOperationStatus
import io.scheduler.comparison.jobrunr.jobs.state.impl.DedicatedJobState
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
        const val READ_UNPROCESSED_ORDER_OPERATIONS_FOR_UPDATE_SQL =  """
                SELECT id, order_id, order_statuses.merchant_id, status_change_time,
                    operation_status AS order_operation_status, record_read_count, order_status
                FROM scheduler_jobrunr.order_statuses
                WHERE
                    operation_status IN ('READY_FOR_PROCESSING', 'FOR_RETRY')
                    AND merchant_id IN (:merchantIds)
                    AND order_statuses.order_status IN (:orderStatuses)
                LIMIT :maxPageSize
                FOR UPDATE SKIP LOCKED
        """

        @Language("PostgreSQL")
        const val UPDATE_SUCCESSFULLY_HANDLED_ORDER_OPERATION_SQL = """
            UPDATE scheduler_jobrunr.order_statuses
            SET
                operation_status = :orderOperationStatus,
                record_read_count = record_read_count + 1,
                status_change_time = :statusChangeTime
            WHERE id IN (:ids)
            RETURNING id, order_id, order_statuses.merchant_id, status_change_time,
                operation_status AS order_operation_status, record_read_count, order_status
        """

        @Language("PostgreSQL")
        const val UPDATE_ORDER_OPERATION_FAILURE_RETRY_SQL = """
            UPDATE scheduler_jobrunr.order_statuses
            SET
                operation_status = CASE
                    WHEN record_read_count < 5 THEN 'FOR_RETRY'
                    ELSE 'RETRIES_EXCEEDED'
                END,
                record_read_count = record_read_count + 1,
                status_change_time = :statusChangeTime
            WHERE id IN (:ids)
            RETURNING id, order_id, order_statuses.merchant_id, status_change_time,
                operation_status AS order_operation_status, record_read_count, order_status
        """

        @Language("PostgreSQL")
        const val UPDATE_ORDER_OPERATION_SET_STATUS_SQL = """
            UPDATE scheduler_jobrunr.order_statuses
            SET
                operation_status = :orderOperationStatus,
                record_read_count = record_read_count + 1,
                status_change_time = :statusChangeTime
            WHERE id IN (:ids)
            RETURNING id, order_id, order_statuses.merchant_id, status_change_time,
                operation_status AS order_operation_status, record_read_count, order_status
        """
    }

    @Transactional(propagation = Propagation.MANDATORY)
    fun readUnprocessedOrders(
        maxPageSize: Int,
        orderJobState: DedicatedJobState
    ): List<OperationOnOrder> {
        val jobData = orderJobState.jobData
        return jdbcClient.sql(READ_UNPROCESSED_ORDER_OPERATIONS_FOR_UPDATE_SQL)
            .param("maxPageSize", maxPageSize)
            .param("merchantIds", jobData.merchantIds)
            .param("orderStatuses", jobData.orderStatuses.asSequence().map { it.value }.toSet())
            .query(OperationOnOrder::class.java)
            .list()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    fun updateOrderOperationsOnSuccess(orderIds: Set<Long>): List<OperationOnOrder>
            = if (orderIds.isNotEmpty()) {
        jdbcClient.sql(UPDATE_SUCCESSFULLY_HANDLED_ORDER_OPERATION_SQL)
            .param("orderOperationStatus", OrderOperationStatus.SENT_TO_NOTIFIER, Types.VARCHAR)
            .param("statusChangeTime", LocalDateTime.now())
            .param("ids", orderIds)
            .query(OperationOnOrder::class.java)
            .list()
    } else emptyList()

    @Transactional(propagation = Propagation.MANDATORY)
    fun markOrderOperationsAsError(orderIds: Set<Long>)
        = if (orderIds.isNotEmpty()) {
            jdbcClient.sql(UPDATE_ORDER_OPERATION_SET_STATUS_SQL)
                .param("orderOperationStatus", OrderOperationStatus.ERROR, Types.VARCHAR)
                .param("ids", orderIds)
                .param("statusChangeTime", LocalDateTime.now())
                .query(OperationOnOrder::class.java)
                .list()
    } else emptyList()

    @Transactional(propagation = Propagation.MANDATORY)
    fun updateOrderOperationsOnFailure(orderIds: Set<Long>): List<OperationOnOrder>
            = if (orderIds.isNotEmpty()) {
        jdbcClient.sql(UPDATE_ORDER_OPERATION_FAILURE_RETRY_SQL)
            .param("statusChangeTime", LocalDateTime.now())
            .param("ids", orderIds)
            .query(OperationOnOrder::class.java)
            .list()
    } else emptyList()

}
