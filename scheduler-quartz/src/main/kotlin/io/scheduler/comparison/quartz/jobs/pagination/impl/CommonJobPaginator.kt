package io.scheduler.comparison.quartz.jobs.pagination.impl

import io.scheduler.comparison.quartz.domain.OperationOnOrder
import io.scheduler.comparison.quartz.jobs.pagination.JobPaginator
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.CommonOrderJobMetadata
import kotlin.math.ceil
import kotlin.math.min

/**
 * An iterator implementation that uses an extractor function to retrieve its values from a data source.
 */
class CommonJobPaginator(
    override val jobData: CommonOrderJobData,
    override val jobMetadata: CommonOrderJobMetadata,
    private val pageExtractor: (pageSize: Long, jobData: CommonOrderJobData) -> List<OperationOnOrder>,
) : JobPaginator<CommonOrderJobData, CommonOrderJobMetadata, OperationOnOrder> {

    override val pageSize: Long = jobMetadata.pageSize

    private var pagesLeft = ceil(jobMetadata.maxCountPerExecution.toDouble() / pageSize).toLong()
    private var elementsLeft = jobMetadata.maxCountPerExecution
    private var elementsQueried = false
    private var currentPage: List<OperationOnOrder> = mutableListOf()

    /**
     * Reads the next page if max page count hasn't been reached yet. It is guaranteed to return the same
     * result until next() is called.
     *
     * Notice that the implementation eagerly queries the next page to determine its presence.
     */
    override fun hasNext(): Boolean {
        if (elementsQueried) {
            return currentPage.isNotEmpty()
        }
        if (pagesLeft <= 0) {
            return false
        }

        // Last page may actually have a stricter limit than pageSize. For example,
        // pageSize=2, maxCountPerExecution=3 should yield only 1 element for page 2 for 4 elements in the data store.
        currentPage = pageExtractor(min(elementsLeft, pageSize), jobData)
        elementsQueried = true
        return currentPage.isNotEmpty()
    }

    /**
     * Queries the next page and returns its contents. If hasNext() was called previously, the cached page
     * will be returned.
     */
    override fun next(): List<OperationOnOrder> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        --pagesLeft
        elementsLeft -= pageSize
        elementsQueried = false
        return currentPage
    }
}
