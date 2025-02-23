package io.scheduler.comparison.dbscheduler.tasks.handlers.pagination

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.dbscheduler.tasks.state.TaskState
import io.scheduler.comparison.dbscheduler.tasks.pagination.TaskPaginator
import io.scheduler.comparison.dbscheduler.tasks.pagination.impl.listTaskPaginator
import io.scheduler.comparison.dbscheduler.tasks.state.data.ChunkedTaskMetadata
import io.scheduler.comparison.dbscheduler.service.TransactionalPaginatedTaskService

abstract class PaginatedTaskHandlerBase<T : TaskState<*, ChunkedTaskMetadata>, V> : PaginatedTaskHandler<T, V> {

    protected abstract val transactionalTaskService: TransactionalPaginatedTaskService<T, V, *>

    protected companion object {
        @JvmStatic
        protected val log = KotlinLogging.logger {}
    }

    override fun run(taskState: T) {
        val paginator = paginator(taskState)

        var totalSent = 0
        var pageSize: Int
        while (handleNextPage(paginator).also { pageSize = it.size }.isNotEmpty()) {
            log.info { "[${taskState.taskMetadata.taskName}] Sent operations: [$pageSize/${taskState.taskMetadata.chunkSize}]" }
            totalSent += pageSize
        }

        log.info { "[${taskState.taskMetadata.taskName}] Completed successfully, $totalSent entries handled." }
    }

    override fun handleNextPage(paginator: TaskPaginator<T, V>)
        = transactionalTaskService.processNextPageTransactionally(paginator)
            .fold<List<V>> (
                ifLeft = { ex -> throw ex },
                ifRight = { operations -> return operations }
            )

    override fun paginator(taskState: T) = listTaskPaginator(
        taskState = taskState,
        pageExtractor = transactionalTaskService.persistentPageRefundExtractor()
    )

}
