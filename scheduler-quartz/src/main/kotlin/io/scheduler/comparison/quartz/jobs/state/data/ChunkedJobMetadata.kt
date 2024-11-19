package io.scheduler.comparison.quartz.jobs.state.data

interface ChunkedJobMetadata : JobMetadata {

    val chunkSize: Int
    val maxCountPerExecution: Int

}