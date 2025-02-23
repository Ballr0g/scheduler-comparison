package io.scheduler.comparison.dbscheduler.tasks.state.data

interface CronTaskMetadata : TaskMetadata {

    val taskCron: String

}