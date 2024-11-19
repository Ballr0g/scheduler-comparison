package io.scheduler.comparison.jobrunr.jobs.state.data.impl

import io.scheduler.comparison.jobrunr.jobs.state.data.ChunkedJobMetadata
import io.scheduler.comparison.jobrunr.jobs.state.data.CronJobMetadata

data class CommonOrderJobMetadata(
    override val jobName: String,
    override val jobCron: String,
    override val chunkSize: Int,
    override val maxCountPerExecution: Int,
) : CronJobMetadata, ChunkedJobMetadata

