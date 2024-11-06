package io.scheduler.comparison.quartz.jobs

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
    MAX_COUNT_PER_EXECUTION("maxCountPerExecution")
}
