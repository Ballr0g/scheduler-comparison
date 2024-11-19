package io.scheduler.comparison.quartz.jobs.handlers.streaming

import io.github.oshai.kotlinlogging.KotlinLogging
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream
import kotlin.streams.asSequence

abstract class ChunkedStreamJobHandlerBase<T, V : ChunkedJobMetadata, K> : StreamBasedJobHandler<T, V, K> {

    protected companion object {
        @JvmStatic
        protected val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun executeInternal(orderJobData: T, orderJobMetadata: V) {
        val availableOperationsStream = openDataStream(orderJobData, orderJobMetadata)

        consumeDataStream(availableOperationsStream, orderJobData, orderJobMetadata)
        log.info { "[${orderJobMetadata.jobName}] Completed successfully" }
    }

    override fun consumeDataStream(stream: Stream<K>, orderJobData: T, orderJobMetadata: V) = stream.use {
        it.asSequence()
            .take(orderJobMetadata.maxCountPerExecution)
            .chunked(orderJobMetadata.chunkSize)
            .forEach { chunk -> handleNextChunk(chunk) }
    }

    abstract fun handleNextChunk(chunk: List<K>)
}
