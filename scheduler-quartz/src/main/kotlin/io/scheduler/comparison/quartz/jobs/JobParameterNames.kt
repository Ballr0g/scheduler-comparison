package io.scheduler.comparison.quartz.jobs

/**
 * Used as a stronger formal contract to match parameter naming during putting params to org.quartz.JobDataMap and
 * extracting back from job context.
 */
enum class DedicatedOrderJobParams(
    val value: String,
) {
    JOB_HANDLER("jobHandler"),
    JOB_STATE("jobState"),
}

/**
 * Used as a stronger formal contract to match parameter naming during putting params to org.quartz.JobDataMap and
 * extracting back from job context.
 */
enum class CommonOrderJobParams(
    val value: String,
) {
    JOB_HANDLER("jobHandler"),
    JOB_STATE("jobState"),
}

object JobHandlerNames {
    const val COMMON_JOB_HANDLER = "commonJobHandler"
    const val LOCA_LOLA_DEDICATED_JOB_HANDLER = "locaLolaDedicatedJobHandler"
    const val WILD_FRUIT_DEDICATED_JOB_HANDLER = "wildFruitDedicatedJobHandler"
}
