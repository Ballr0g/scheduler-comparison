package io.scheduler.comparison.quartz.jobs.state

data class DedicatedOrderJobMetadata(
    override val jobName: String,
    override val jobCron: String,
    override val pageSize: Long,
    override val maxCountPerExecution: Long,
) : CronJobMetadata, PaginatedJobMetadata
