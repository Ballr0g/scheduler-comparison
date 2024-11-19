package io.scheduler.comparison.quartz.jobs.handlers.pagination

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.jobs.state.JobState
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata
import org.springframework.transaction.annotation.Transactional

abstract class PaginatedJobHandlerBase<T : JobState<*, ChunkedJobMetadata>, V> : PaginatedJobHandler<T, V> {

    protected companion object {
        @JvmStatic
        protected val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun executeInternal(orderJobState: T) {
        val paginator = paginator(orderJobState)

        if (!paginator.hasNext()) {
            log.info { "[${orderJobState.jobMetadata.jobName}] No new entries available, execution completed" }
            return
        }

        // Todo: fetch pages inside the same transaction.
        paginator.forEach { handleNextPage(it) }
        log.info { "[${orderJobState.jobMetadata.jobName}] Completed successfully" }
    }

}
