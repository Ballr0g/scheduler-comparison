package io.scheduler.comparison.quartz.jobs.pagination.impl

import io.scheduler.comparison.quartz.jobs.pagination.JobPaginator
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata
import kotlin.math.ceil
import kotlin.math.min

/**
 * An iterator implementation that uses an extractor function to retrieve its values from a data source as a List.
 */
class ListJobPaginator<out T, V : ChunkedJobMetadata, K> private constructor(
    override val jobData: T,
    override val jobMetadata: V,
    private val pageExtractor: (pageSize: Int, jobData: T) -> List<K>,
) : JobPaginator<T, V, K> {

    data class Builder<T, V : ChunkedJobMetadata, K>(
        var jobData: T,
        var jobMetadata: V,
        var pageExtractor: (pageSize: Int, jobData: T) -> List<K>,
    ) {

        fun build() = ListJobPaginator(jobData, jobMetadata, pageExtractor)

    }

    override val pageSize: Int = jobMetadata.chunkSize

    private var pagesLeft = ceil(jobMetadata.maxCountPerExecution.toDouble() / pageSize).toLong()
    private var elementsLeft = jobMetadata.maxCountPerExecution
    private var elementsQueried = false
    private var currentPage: List<K> = mutableListOf()

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
    override fun next(): List<K> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        --pagesLeft
        elementsLeft -= pageSize
        elementsQueried = false
        return currentPage
    }
}

fun <T, V : ChunkedJobMetadata, K> listJobPaginator(
    jobData: T,
    jobMetadata: V,
    pageExtractor: (pageSize: Int, jobData: T) -> List<K>,
    buildActions: (ListJobPaginator.Builder<T, V, K>.() -> Unit)? = null
) = ListJobPaginator.Builder(jobData, jobMetadata, pageExtractor).apply(buildActions ?: {}).build()
