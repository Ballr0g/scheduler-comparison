package io.scheduler.comparison.jobrunr.jobs.pagination

import io.scheduler.comparison.jobrunr.jobs.state.JobState
import io.scheduler.comparison.jobrunr.jobs.state.data.ChunkedJobMetadata

interface JobPaginator<T : JobState<*, ChunkedJobMetadata>, V> : Iterator<List<V>> {

    val jobState: T
    val pageSize: Int

}
