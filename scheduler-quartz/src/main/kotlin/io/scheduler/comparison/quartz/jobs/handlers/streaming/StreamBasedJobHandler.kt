package io.scheduler.comparison.quartz.jobs.handlers.streaming

import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.state.JobMetadata
import java.util.stream.Stream

interface StreamBasedJobHandler<T, V : JobMetadata, K> : JobHandler<T, V> {

    fun consumeDataStream(
        stream: Stream<K>,
        orderJobData: T,
        orderJobMetadata: V,
    )

    fun openDataStream(orderJobData: T, orderJobMetadata: V): Stream<K>

}
