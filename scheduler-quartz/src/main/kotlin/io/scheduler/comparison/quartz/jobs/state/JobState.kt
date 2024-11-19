package io.scheduler.comparison.quartz.jobs.state

import io.scheduler.comparison.quartz.jobs.state.data.JobMetadata

interface JobState<out T, out V : JobMetadata> {

    val jobData: T
    val jobMetadata: V

}
