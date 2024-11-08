package io.scheduler.comparison.quartz.jobs.state

data class DedicatedOrderJobMetadata(
    override val jobName: String,
    override val jobCron: String,
    override val chunkSize: Int,
    override val maxCountPerExecution: Int,
) : CronJobMetadata, ChunkedJobMetadata
