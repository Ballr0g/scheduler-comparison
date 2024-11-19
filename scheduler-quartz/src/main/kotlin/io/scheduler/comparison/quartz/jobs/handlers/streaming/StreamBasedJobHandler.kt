package io.scheduler.comparison.quartz.jobs.handlers.streaming

import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.state.JobState
import java.util.stream.Stream

interface StreamBasedJobHandler<T : JobState<*, *>, V> : JobHandler<T> {

    fun consumeDataStream(stream: Stream<V>, orderJobState: T)

    fun openDataStream(orderJobState: T): Stream<V>

}
