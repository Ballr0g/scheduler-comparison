package io.scheduler.comparison.quartz.jobs.state.impl

import io.scheduler.comparison.quartz.jobs.state.JobState
import io.scheduler.comparison.quartz.jobs.state.data.impl.DedicatedOrderJobData
import io.scheduler.comparison.quartz.jobs.state.data.impl.DedicatedOrderJobMetadata

class DedicatedJobState(
    override val jobData: DedicatedOrderJobData,
    override val jobMetadata: DedicatedOrderJobMetadata
) : JobState<DedicatedOrderJobData, DedicatedOrderJobMetadata>
