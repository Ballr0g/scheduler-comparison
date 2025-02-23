package io.scheduler.comparison.dbscheduler.tasks.state.data

import java.io.Serializable

interface TaskMetadata : Serializable {

    val taskName: String

}