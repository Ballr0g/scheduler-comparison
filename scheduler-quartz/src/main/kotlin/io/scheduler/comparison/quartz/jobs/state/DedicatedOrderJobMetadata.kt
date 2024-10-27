package io.scheduler.comparison.quartz.jobs.state

data class DedicatedOrderJobMetadata(
    val jobName: String,
    val jobCron: String,
    val pageSize: Long,
    val maxCountPerExecution: Long,
)
