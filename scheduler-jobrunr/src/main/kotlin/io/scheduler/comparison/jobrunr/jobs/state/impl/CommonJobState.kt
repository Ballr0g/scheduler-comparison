package io.scheduler.comparison.jobrunr.jobs.state.impl

import io.scheduler.comparison.jobrunr.jobs.state.JobState
import io.scheduler.comparison.jobrunr.jobs.state.data.impl.CommonOrderJobData
import io.scheduler.comparison.jobrunr.jobs.state.data.impl.CommonOrderJobMetadata

data class CommonJobState(
    override val jobData: CommonOrderJobData,
    override val jobMetadata: CommonOrderJobMetadata,
) : JobState<CommonOrderJobData, CommonOrderJobMetadata>
