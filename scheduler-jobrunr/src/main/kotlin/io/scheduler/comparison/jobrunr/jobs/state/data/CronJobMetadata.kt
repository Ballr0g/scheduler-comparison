package io.scheduler.comparison.jobrunr.jobs.state.data

interface CronJobMetadata : JobMetadata {

    val jobCron: String

}