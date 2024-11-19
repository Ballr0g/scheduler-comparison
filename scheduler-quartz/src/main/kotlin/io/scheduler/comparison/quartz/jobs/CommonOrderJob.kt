package io.scheduler.comparison.quartz.jobs

import io.scheduler.comparison.quartz.jobs.handlers.JobHandler
import io.scheduler.comparison.quartz.jobs.state.impl.CommonJobState
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A general-purpose job execution class that works with all merchants except for those used for dedicated jobs.
 */
@Component
class CommonOrderJob : HandlerAwareJob<CommonJobState> {
    @Autowired
    private lateinit var jobHandlers: Map<String, JobHandler<CommonJobState>>

    override fun execute(context: JobExecutionContext) {
        try {
            val jobDataMap = context.jobDetail.jobDataMap
            val jobState = retrieveJobState(jobDataMap)
            val jobHandler = retrieveJobHandlerByKey(jobDataMap.getString(CommonOrderJobParams.JOB_HANDLER.value))

            jobHandler.executeInternal(jobState)
        } catch (e: Exception) {
            throw JobExecutionException(e)
        }
    }

    override fun retrieveJobState(jobDataMap: JobDataMap)
        = jobDataMap[CommonOrderJobParams.JOB_STATE.value] as? CommonJobState
            ?: throw IllegalStateException("Unable to deserialize CommonJobState by key: "
                    + CommonOrderJobParams.JOB_STATE.value)

    override fun retrieveJobHandlerByKey(jobKey: String)
        = jobHandlers[jobKey]
            ?: throw IllegalStateException("Unsupported jobHandler=${jobKey}, " +
                "available options: ${jobHandlers.keys}")

}
