package io.scheduler.comparison.quartz.jobs.state.data

interface CronJobMetadata : JobMetadata {

    val jobCron: String

}