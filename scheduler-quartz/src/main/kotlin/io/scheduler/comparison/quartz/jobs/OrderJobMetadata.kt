package io.scheduler.comparison.quartz.jobs

data class OrderJobMetadata(
    val jobName: String,
    val jobCron: String,
)
