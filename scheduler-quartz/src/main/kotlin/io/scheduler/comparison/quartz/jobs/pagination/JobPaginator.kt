package io.scheduler.comparison.quartz.jobs.pagination

import io.scheduler.comparison.quartz.jobs.state.JobState
import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata

interface JobPaginator<T : JobState<*, ChunkedJobMetadata>, V>  : Iterator<List<V>> {

    val jobState: T
    val pageSize: Int

}
