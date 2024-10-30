package io.scheduler.comparison.quartz.jobs.pagination

interface JobPaginator<T, V, K> : Iterator<List<K>> {

    val jobData: T
    val jobMetadata: V
    val pageSize: Long

}
