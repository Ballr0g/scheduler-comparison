package io.scheduler.comparison.quartz.jobs.pagination

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.DedicatedOrderJobMetadata
import kotlin.math.ceil

class DedicatedJobPaginator(
    override val jobData: DedicatedOrderJobData,
    override val jobMetadata: DedicatedOrderJobMetadata,
    private val pageExtractor: (pageSize: Long, jobData: DedicatedOrderJobData) -> List<OperationOnOrder>,
) : JobPaginator<DedicatedOrderJobData, DedicatedOrderJobMetadata, OperationOnOrder> {

    override val pageSize: Long = jobMetadata.pageSize

    private var pagesLeft = ceil(jobMetadata.maxCountPerExecution.toDouble() / pageSize).toLong()
    private var elementsQueried = false
    private var currentPage: List<OperationOnOrder> = mutableListOf()

    override fun hasNext(): Boolean {
        if (elementsQueried) {
            return currentPage.isNotEmpty()
        }
        if (pagesLeft <= 0) {
            return false
        }

        currentPage = pageExtractor(pageSize, jobData)
        elementsQueried = true
        return currentPage.isNotEmpty()
    }

    override fun next(): List<OperationOnOrder> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        --pagesLeft
        elementsQueried = false
        return currentPage
    }
}
