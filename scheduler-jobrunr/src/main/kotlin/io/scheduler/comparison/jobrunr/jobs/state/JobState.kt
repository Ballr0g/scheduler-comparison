package io.scheduler.comparison.jobrunr.jobs.state

import io.scheduler.comparison.jobrunr.jobs.state.data.JobMetadata

interface JobState<out T, out V : JobMetadata> {

    val jobData: T
    val jobMetadata: V

}
