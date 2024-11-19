package io.scheduler.comparison.quartz.jobs.handlers.pagination

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata
import org.springframework.transaction.annotation.Transactional

abstract class PaginatedJobHandlerBase<T, V : ChunkedJobMetadata, K> : PaginatedJobHandler<T, V, K> {

    protected companion object {
        @JvmStatic
        protected val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun executeInternal(orderJobData: T, orderJobMetadata: V) {
        val paginator = paginator(orderJobData, orderJobMetadata)

        if (!paginator.hasNext()) {
            log.info { "[${orderJobMetadata.jobName}] No new entries available, execution completed" }
            return
        }

        // Todo: fetch pages inside the same transaction.
        paginator.forEach { handleNextPage(it) }
        log.info { "[${orderJobMetadata.jobName}] Completed successfully" }
    }

}
