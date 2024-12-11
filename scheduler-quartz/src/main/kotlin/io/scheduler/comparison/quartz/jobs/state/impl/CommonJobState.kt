package io.scheduler.comparison.quartz.jobs.state.impl

import io.scheduler.comparison.quartz.jobs.state.JobState
import io.scheduler.comparison.quartz.jobs.state.data.impl.CommonOrderJobData
import io.scheduler.comparison.quartz.jobs.state.data.impl.CommonOrderJobMetadata

data class CommonJobState(
    override val jobData: CommonOrderJobData,
    override val jobMetadata: CommonOrderJobMetadata,
) : JobState<CommonOrderJobData, CommonOrderJobMetadata>
