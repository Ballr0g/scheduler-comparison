package io.scheduler.comparison.quartz.jobs.pagination

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.domain.OrderOperationStatus
import io.scheduler.comparison.quartz.domain.OrderStatus
import io.scheduler.comparison.quartz.jobs.pagination.impl.listJobPaginator
import io.scheduler.comparison.quartz.jobs.state.data.impl.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.data.impl.CommonOrderJobMetadata
import io.scheduler.comparison.quartz.jobs.state.impl.CommonJobState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.util.*
import kotlin.NoSuchElementException


class ListJobPaginatorTest {

    private companion object {
        val testCommonJobState = CommonJobState(
            jobData = CommonOrderJobData(
                excludedMerchantIds = setOf(4, 5),
                orderStatuses = setOf(OrderStatus.PAID, OrderStatus.FAILED, OrderStatus.CANCELLED, OrderStatus.DELIVERED)
            ),
            jobMetadata = CommonOrderJobMetadata(
                jobName = "test-job",
                jobCron = "0 */1 * * * ?",
                chunkSize = 1,
                maxCountPerExecution = 2
            )
        )

        val testOperationOnOrder1 = OperationOnOrder(
            id = 1,
            orderId = UUID.fromString("2dfed551-a49b-4f91-8907-ec8b493d0480"),
            merchantId = 4,
            statusChangeTime = LocalDateTime.of(LocalDate.of(2024, Month.OCTOBER, 30), LocalTime.NOON),
            orderOperationStatus = OrderOperationStatus.READY_FOR_PROCESSING,
            recordReadCount = 0,
            orderStatus = OrderStatus.PAID,
        )

        val testOperationOnOrder2 = OperationOnOrder(
            id = 2,
            orderId = UUID.fromString("2dfed551-a49b-4f91-8907-ec8b493d0480"),
            merchantId = 4,
            statusChangeTime = LocalDateTime.of(LocalDate.of(2024, Month.OCTOBER, 30), LocalTime.NOON.minusSeconds(30)),
            orderOperationStatus = OrderOperationStatus.READY_FOR_PROCESSING,
            recordReadCount = 0,
            orderStatus = OrderStatus.CANCELLED,
        )

        val testOperationsOnOrder = listOf(testOperationOnOrder1, testOperationOnOrder2)
    }

    @Test
    fun `Empty ListJobPaginator hasNext() returns false`() {
        // given
        val jobPaginator = listJobPaginator(testCommonJobState, { _, _ -> emptyList<Long>() })

        // when
        // then
        assertFalse(jobPaginator.hasNext())
    }

    @Test
    fun `Empty ListJobPaginator next() throws NoSuchElementException`() {
        // given
        val jobPaginator = listJobPaginator(testCommonJobState, { _, _ -> emptyList<Long>() })

        // when
        // then
        assertThrows<NoSuchElementException> { jobPaginator.next() }
    }

    @Test
    fun `Single page for ListJobPaginator hasNext() returns true on first call`() {
        // given
        val jobPaginator = listJobPaginator(testCommonJobState, { _, _ -> testOperationsOnOrder })

        // when
        // then
        assertTrue(jobPaginator.hasNext())
    }

    @Test
    fun `Multiple pages for ListJobPaginator next() return expected value on first call`() {
        // given
        val jobPaginator = listJobPaginator(testCommonJobState, { pageSize, _ -> testOperationsOnOrder.take(pageSize) })

        // when
        val actualPage = jobPaginator.next()

        // then
        assertEquals(testCommonJobState.jobMetadata.chunkSize, actualPage.size)
        assertEquals(testOperationOnOrder1, actualPage[0])
    }

    @Test
    fun `Multiple pages for ListJobPaginator hasNext() returns true when second page exists`() {
        // given
        var currentPage = 0
        val jobPaginator = listJobPaginator(testCommonJobState,
            { _, _ -> listOf(testOperationsOnOrder[currentPage++]) }
        )

        // when
        jobPaginator.next()

        // then
        assertTrue(jobPaginator.hasNext())
    }

    @Test
    fun `Multiple pages for ListJobPaginator next() returns expected second page when it exists`() {
        // given
        var currentPage = 0
        val jobPaginator = listJobPaginator(testCommonJobState,
            { _, _ -> listOf(testOperationsOnOrder[currentPage++]) }
        )

        // when
        jobPaginator.next()
        val actualSecondPage = jobPaginator.next()

        // then
        assertEquals(testCommonJobState.jobMetadata.chunkSize, actualSecondPage.size)
        assertEquals(testOperationOnOrder2, actualSecondPage[0])
    }

    @Test
    fun `Page size does not exceed max entries count when more data available than queried via next()`() {
        // given
        val jobStateExcessiveMetadataPageSize = CommonJobState(
            jobData = testCommonJobState.jobData,
            jobMetadata = testCommonJobState.jobMetadata.copy(
                chunkSize = 2,
                maxCountPerExecution = 1
            )
        )
        val jobPaginator = listJobPaginator(jobStateExcessiveMetadataPageSize,
            { pageSize, _ -> testOperationsOnOrder.take(pageSize) }
        )

        // when
        val actualPage = jobPaginator.next()

        // then
        assertEquals(jobStateExcessiveMetadataPageSize.jobMetadata.maxCountPerExecution, actualPage.size)
        assertEquals(testOperationOnOrder1, actualPage[0])
    }

}