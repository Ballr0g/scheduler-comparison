package io.scheduler.comparison.quartz.jobs

import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.state.impl.DedicatedJobState
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Dedicated job class instances are used to process only the merchants with specified IDs during initialization.
 */
@Component
class DedicatedOrderJob : HandlerAwareJob<DedicatedJobState> {
    @Autowired
    private lateinit var jobHandlers: Map<String, JobHandler<DedicatedJobState>>

    override fun execute(context: JobExecutionContext) {
        try {
            val jobDataMap = context.jobDetail.jobDataMap
            val jobState = retrieveJobState(jobDataMap)
            val jobHandler = retrieveJobHandlerByKey(jobDataMap.getString(DedicatedOrderJobParams.JOB_HANDLER.value))

            jobHandler.executeInternal(jobState)
        } catch (e: Exception) {
            throw JobExecutionException(e)
        }
    }

    override fun retrieveJobState(jobDataMap: JobDataMap)
            = jobDataMap[DedicatedOrderJobParams.JOB_STATE.value] as? DedicatedJobState
        ?: throw IllegalStateException("Unable to deserialize DedicatedJobState by key: "
                + DedicatedOrderJobParams.JOB_STATE.value)

    override fun retrieveJobHandlerByKey(jobKey: String)
            = jobHandlers[jobKey]
        ?: throw IllegalStateException("Unsupported jobHandler=${jobKey}, " +
                "available options: ${jobHandlers.keys}")

}
