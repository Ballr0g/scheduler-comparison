package io.scheduler.comparison.jobrunr.jobs.state.data

interface ChunkedJobMetadata : JobMetadata {

    val chunkSize: Int
    val maxCountPerExecution: Int

}