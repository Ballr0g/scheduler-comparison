package io.scheduler.comparison.quartz.repositories.streaming

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.domain.OrderOperationStatus
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobMetadata
import io.scheduler.comparison.quartz.repositories.DomainRowMappers.operationOnOrderRowMapper
import org.intellij.lang.annotations.Language
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.stream.Stream

@Repository
@Profile("streaming")
class CommonStreamingOperationOnOrderRepository(
    private val jdbcOperations: NamedParameterJdbcOperations
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
                FOR UPDATE SKIP LOCKED
                LIMIT :maxCount
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

    fun readUnprocessedOperations(
        orderJobData: CommonOrderJobData,
        orderJobMetadata: CommonOrderJobMetadata,
    ): Stream<OperationOnOrder> = jdbcOperations.queryForStream(
        READ_UNPROCESSED_ORDER_OPERATIONS_FOR_UPDATE_SQL,
        mapOf(
            "excludedMerchantIds" to orderJobData.excludedMerchantIds.toTypedArray(),
            "orderStatuses" to orderJobData.orderStatuses.asSequence().map { it.value }.toSet(),
            "maxCount" to orderJobMetadata.maxCountPerExecution,
        ), operationOnOrderRowMapper
    )

    fun incrementOperationsReadCount(availableOperations: List<OperationOnOrder>): List<OperationOnOrder> {
        // This might happen when the database is either empty or the entries are still locked by another action.
        if (availableOperations.isEmpty()) {
            return emptyList()
        }

        return jdbcOperations.query(
            INCREASE_UNPROCESSED_ORDER_OPERATION_READ_COUNT_SQL,
            mapOf("ids" to availableOperations.map { it.id }.toSet()),
            operationOnOrderRowMapper
        )
    }

    fun markOrderOperationsAsProcessed(orderIds: Set<Long>)
        = if (orderIds.isNotEmpty()) {
            jdbcOperations.update(
                CHANGE_ORDER_OPERATION_STATUSES_SQL,
                mapOf(
                    "orderOperationStatus" to OrderOperationStatus.SENT_TO_NOTIFIER.name,
                    "statusChangeTime" to LocalDateTime.now(),
                    "ids" to orderIds,
            ))
    } else 0
}
