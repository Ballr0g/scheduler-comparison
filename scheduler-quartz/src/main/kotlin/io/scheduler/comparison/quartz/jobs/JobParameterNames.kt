package io.scheduler.comparison.quartz.jobs

// Todo: consider reusing the idea of JobRunr serializable "JobRequest" and use them instead of raw properties.
/**
 * Used as a stronger formal contract to match parameter naming during putting params to org.quartz.JobDataMap and
 * extracting back from job context.
 */
enum class DedicatedOrderJobParams(
    val value: String,
) {
    JOB_NAME("name"),
    MERCHANT_IDS("merchantIds"),
    ORDER_STATUSES("orderStatuses"),
    JOB_CRON("cron"),
    PAGE_SIZE("pageSize"),
    MAX_COUNT_PER_EXECUTION("maxCountPerExecution"),
    JOB_HANDLER("jobHandler"),
}

/**
 * Used as a stronger formal contract to match parameter naming during putting params to org.quartz.JobDataMap and
 * extracting back from job context.
 */
enum class CommonOrderJobParams(
    val value: String,
) {
    JOB_NAME("name"),
    EXCLUDED_MERCHANT_IDS("excludedMerchantIds"),
    ORDER_STATUSES("orderStatuses"),
    JOB_CRON("cron"),
    PAGE_SIZE("pageSize"),
    MAX_COUNT_PER_EXECUTION("maxCountPerExecution"),
    JOB_HANDLER("jobHandler"),
}

object JobHandlerNames {
    const val COMMON_JOB_HANDLER = "commonJobHandler"
    const val LOCA_LOLA_DEDICATED_JOB_HANDLER = "locaLolaDedicatedJobHandler"
    const val WILD_FRUIT_DEDICATED_JOB_HANDLER = "wildFruitDedicatedJobHandler"
}
