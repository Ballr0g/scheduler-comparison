package io.scheduler.comparison.quartz.jobs.handlers

fun interface JobHandler<T, V> {

    fun executeInternal(orderJobData: T, orderJobMetadata: V)

}