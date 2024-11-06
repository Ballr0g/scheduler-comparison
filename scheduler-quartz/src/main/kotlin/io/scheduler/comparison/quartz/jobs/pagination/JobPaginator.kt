package io.scheduler.comparison.quartz.jobs.pagination

import io.scheduler.comparison.quartz.jobs.state.PaginatedJobMetadata

interface JobPaginator<out T , out V : PaginatedJobMetadata, K> : Iterator<List<K>> {

    val jobData: T
    val jobMetadata: V
    val pageSize: Long

}
