package io.scheduler.comparison.jobrunr.jobs.state.impl

import io.scheduler.comparison.jobrunr.jobs.state.JobState
import io.scheduler.comparison.jobrunr.jobs.state.data.impl.DedicatedOrderJobData
import io.scheduler.comparison.jobrunr.jobs.state.data.impl.DedicatedOrderJobMetadata

class DedicatedJobState(
    override val jobData: DedicatedOrderJobData,
    override val jobMetadata: DedicatedOrderJobMetadata
) : JobState<DedicatedOrderJobData, DedicatedOrderJobMetadata>
