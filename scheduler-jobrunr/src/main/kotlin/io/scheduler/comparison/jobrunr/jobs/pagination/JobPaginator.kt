package io.scheduler.comparison.jobrunr.jobs.pagination

import io.scheduler.comparison.jobrunr.jobs.state.JobState

interface JobPaginator<T : JobState<*, *>, V> : Iterator<List<V>> {

    val jobState: T
    val pageSize: Int

}
