package io.scheduler.comparison.quartz.jobs.state

interface CronJobMetadata : JobMetadata {

    val jobCron: String

}