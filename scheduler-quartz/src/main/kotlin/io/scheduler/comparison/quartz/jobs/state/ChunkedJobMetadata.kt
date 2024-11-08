package io.scheduler.comparison.quartz.jobs.state

interface ChunkedJobMetadata : JobMetadata {

    val chunkSize: Int
    val maxCountPerExecution: Int

}