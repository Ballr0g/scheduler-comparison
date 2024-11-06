package io.scheduler.comparison.quartz.jobs.state

interface PaginatedJobMetadata : JobMetadata {

    val pageSize: Long
    val maxCountPerExecution: Long

}