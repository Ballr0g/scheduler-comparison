package io.scheduler.comparison.quartz.jobs

import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.state.JobState
import org.quartz.Job
import org.quartz.JobDataMap

/**
 * An interface allowing support for dynamically retrieving a handler implementation within the same Job class.
 */
interface HandlerAwareJob<T : JobState<*, *>> : Job {

    fun retrieveJobState(jobDataMap: JobDataMap): T

    fun retrieveJobHandlerByKey(jobKey: String): JobHandler<T>

}
