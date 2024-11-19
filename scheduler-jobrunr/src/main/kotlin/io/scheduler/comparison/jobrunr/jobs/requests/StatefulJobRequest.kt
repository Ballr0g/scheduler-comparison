package io.scheduler.comparison.jobrunr.jobs.requests

import io.scheduler.comparison.jobrunr.jobs.state.JobState
import io.scheduler.comparison.jobrunr.jobs.state.data.JobMetadata
import org.jobrunr.jobs.lambdas.JobRequest

interface StatefulJobRequest<T : JobState<*, JobMetadata>> : JobRequest {

    val jobState: T

}