package io.scheduler.comparison.quartz.jobs.state

interface PaginatedJobMetadata : JobMetadata {

    val pageSize: Int
    val maxCountPerExecution: Int

}