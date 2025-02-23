package io.scheduler.comparison.dbscheduler.tasks.pagination.impl

import io.scheduler.comparison.dbscheduler.tasks.pagination.TaskPaginator
import io.scheduler.comparison.dbscheduler.tasks.state.TaskState
import io.scheduler.comparison.dbscheduler.tasks.state.data.ChunkedTaskMetadata
import kotlin.math.ceil
import kotlin.math.min

/**
 * An iterator implementation that uses an extractor function to retrieve its values from a data source as a List.
 */
class ListTaskPaginator<T : TaskState<*, ChunkedTaskMetadata>, V> private constructor(
    override val taskState: T,
    private val pageExtractor: (pageSize: Int, taskState: T) -> List<V>,
) : TaskPaginator<T, V> {

    data class Builder<T : TaskState<*, ChunkedTaskMetadata>, V>(
        var taskState: T,
        var pageExtractor: (pageSize: Int, taskState: T) -> List<V>,
    ) {

        fun build() = ListTaskPaginator(taskState, pageExtractor)

    }

    override val pageSize: Int = taskState.taskMetadata.chunkSize

    private var pagesLeft = ceil(taskState.taskMetadata.maxCountPerExecution.toDouble() / pageSize).toLong()
    private var elementsLeft = taskState.taskMetadata.maxCountPerExecution
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
        currentPage = pageExtractor(min(elementsLeft, pageSize), taskState)
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

fun <T : TaskState<*, ChunkedTaskMetadata>, V> listTaskPaginator(
    taskState: T,
    pageExtractor: (pageSize: Int, taskState: T) -> List<V>,
    buildActions: (ListTaskPaginator.Builder<T, V>.() -> Unit)? = null
) = ListTaskPaginator.Builder(taskState, pageExtractor).apply(buildActions ?: {}).build()
