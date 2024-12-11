package io.scheduler.comparison.quartz.jobs.pagination.impl

import io.scheduler.comparison.quartz.jobs.pagination.JobPaginator
import io.scheduler.comparison.quartz.jobs.state.JobState
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata
import kotlin.math.ceil
import kotlin.math.min

/**
 * An iterator implementation that uses an extractor function to retrieve its values from a data source as a List.
 */
class ListJobPaginator<T : JobState<*, ChunkedJobMetadata>, V> private constructor(
    override val jobState: T,
    private val pageExtractor: (pageSize: Int, jobData: T) -> List<V>,
) : JobPaginator<T, V> {

    data class Builder<T : JobState<*, ChunkedJobMetadata>, V>(
        var jobState: T,
        var pageExtractor: (pageSize: Int, jobData: T) -> List<V>,
    ) {

        fun build() = ListJobPaginator(jobState, pageExtractor)

    }

    override val pageSize: Int = jobState.jobMetadata.chunkSize

    private var pagesLeft = ceil(jobState.jobMetadata.maxCountPerExecution.toDouble() / pageSize).toLong()
    private var elementsLeft = jobState.jobMetadata.maxCountPerExecution
    private var elementsQueried = false
    private var currentPage: List<V> = mutableListOf()

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
        currentPage = pageExtractor(min(elementsLeft, pageSize), jobState)
        elementsQueried = true
        return currentPage.isNotEmpty()
    }

    /**
     * Queries the next page and returns its contents. If hasNext() was called previously, the cached page
     * will be returned.
     */
    override fun next(): List<V> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        --pagesLeft
        elementsLeft -= pageSize
        elementsQueried = false
        return currentPage
    }
}

fun <T : JobState<*, ChunkedJobMetadata>, V> listJobPaginator(
    jobState: T,
    pageExtractor: (pageSize: Int, jobData: T) -> List<V>,
    buildActions: (ListJobPaginator.Builder<T, V>.() -> Unit)? = null
) = ListJobPaginator.Builder(jobState, pageExtractor).apply(buildActions ?: {}).build()
