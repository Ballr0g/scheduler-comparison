package io.scheduler.comparison.quartz.jobs.pagination

import io.scheduler.comparison.quartz.jobs.state.data.ChunkedJobMetadata

interface JobPaginator<out T , out V : ChunkedJobMetadata, K> : Iterator<List<K>> {

    val jobData: T
    val jobMetadata: V
    val pageSize: Int

}
