package io.scheduler.comparison.quartz.jobs.handlers.streaming

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.jobs.state.JobState
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream
import kotlin.streams.asSequence

abstract class ChunkedStreamJobHandlerBase<T : JobState<*, ChunkedJobMetadata>, V> : StreamBasedJobHandler<T, V> {

    protected companion object {
        @JvmStatic
        protected val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun executeInternal(orderJobState: T) {
        val availableOperationsStream = openDataStream(orderJobState)

        consumeDataStream(availableOperationsStream, orderJobState)
        log.info { "[${orderJobState.jobMetadata.jobName}] Completed successfully" }
    }

    override fun consumeDataStream(stream: Stream<V>, orderJobState: T) = stream.use {
        val jobMetadata = orderJobState.jobMetadata
        it.asSequence()
            .take(jobMetadata.maxCountPerExecution)
            .chunked(jobMetadata.chunkSize)
            .forEach { chunk -> handleNextChunk(chunk) }
    }

    abstract fun handleNextChunk(chunk: List<V>)
}
