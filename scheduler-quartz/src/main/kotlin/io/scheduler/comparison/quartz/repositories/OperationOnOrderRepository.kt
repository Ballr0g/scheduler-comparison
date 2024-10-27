package io.scheduler.comparison.quartz.repositories

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class OperationOnOrderRepository(
    private val jdbcClient: JdbcClient
) {

    private companion object {
        @Language("PostgreSQL")
        const val READ_UNPROCESSED_ORDER_OPERATIONS_SQL =  """
                SELECT id, order_id, status_change_time, operation_status AS order_operation_status, record_read_count, order_status
                FROM scheduler_quartz.order_statuses
                WHERE operation_status IN ('READY_FOR_PROCESSING', 'FOR_RETRY')
                LIMIT :maxPageSize
        """
    }

    fun readUnprocessed(maxPageSize: Long): List<OperationOnOrder>
    = jdbcClient.sql(READ_UNPROCESSED_ORDER_OPERATIONS_SQL)
        .param("maxPageSize", maxPageSize)
        .query(OperationOnOrder::class.java)
        .list()

}
