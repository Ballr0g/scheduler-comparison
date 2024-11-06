package io.scheduler.comparison.quartz.jobs.handlers

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.jobs.state.PaginatedJobMetadata
import org.springframework.transaction.annotation.Transactional

abstract class PaginatedJobHandlerBase<T, V : PaginatedJobMetadata, K> : PaginatedJobHandler<T, V, K> {

    protected companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun executeInternal(orderJobData: T, orderJobMetadata: V) {
        val paginator = paginator(orderJobData, orderJobMetadata)

        if (!paginator.hasNext()) {
            log.info { "[${orderJobMetadata.jobName}] No new entries available, execution completed" }
            return
        }

        paginator.forEach { handleNextPage(it) }
        log.info { "[${orderJobMetadata.jobName}] Completed successfully" }
    }

}
