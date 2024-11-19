package io.scheduler.comparison.quartz.jobs.handlers

import io.scheduler.comparison.quartz.jobs.state.JobMetadata

// Todo: replace JobData/JobMetadata with JobState aggregate interface
fun interface JobHandler<T, V : JobMetadata> {

    fun executeInternal(orderJobData: T, orderJobMetadata: V)

}