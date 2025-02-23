package io.scheduler.comparison.dbscheduler.tasks.state.data.impl

import io.scheduler.comparison.dbscheduler.tasks.state.data.ChunkedTaskMetadata
import io.scheduler.comparison.dbscheduler.tasks.state.data.CronTaskMetadata

data class CommonOrderTaskMetadata(
    // db-scheduler requires a constructor without parameters to properly work with deserialization.
    override val taskName: String = "",
    override val taskCron: String = "",
    override val chunkSize: Int = 0,
    override val maxCountPerExecution: Int = 0,
) : CronTaskMetadata, ChunkedTaskMetadata

