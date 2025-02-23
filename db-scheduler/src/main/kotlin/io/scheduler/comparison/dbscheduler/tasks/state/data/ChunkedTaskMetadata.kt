package io.scheduler.comparison.dbscheduler.tasks.state.data

interface ChunkedTaskMetadata : TaskMetadata {

    val chunkSize: Int
    val maxCountPerExecution: Int

}