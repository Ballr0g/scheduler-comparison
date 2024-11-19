package io.scheduler.comparison.quartz.jobs.handlers

import io.scheduler.comparison.quartz.jobs.state.JobState

fun interface JobHandler<T : JobState<*, *>> {

    fun executeInternal(orderJobState: T)

}