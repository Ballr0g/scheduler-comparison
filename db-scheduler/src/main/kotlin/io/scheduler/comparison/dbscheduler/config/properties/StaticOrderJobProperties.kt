package io.scheduler.comparison.dbscheduler.config.properties

import io.scheduler.comparison.dbscheduler.domain.OrderStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "scheduler.jobs")
data class StaticOrderJobProperties(
    val dedicatedMerchantJobs: List<StaticDedicatedMerchantsOrderJob> = emptyList(),
    val commonMerchantJobs: List<StaticCommonOrderJob> = emptyList(),
){

    @Validated
    data class StaticCommonOrderJob(
        @field:NotBlank
        val name: String,
        @field:Size(min = 1, max = 1000)
        val orderStatuses: List<OrderStatus>,
        @field:NotBlank
        val jobHandler: String = "commonJobHandler",
        @field:NotBlank
        val cron: String,
        @field:Min(1)
        @field:Max(1000)
        val pageSize: Int,
        @field:Min(1)
        @field:Max(1000)
        val maxCountPerExecution: Int,
    )

    @Validated
    data class StaticDedicatedMerchantsOrderJob(
        @field:NotBlank
        val name: String,
        @field:Size(min = 1, max = 1000)
        val merchantIds: List<Long>,
        @field:Size(min = 1, max = 1000)
        val orderStatuses: List<OrderStatus>,
        @field:NotBlank
        val cron: String,
        val ignoredByCommon: Boolean = true,
        @field:NotBlank
        val jobHandler: String,
        @field:Min(1)
        @field:Max(1000)
        val pageSize: Int,
        @field:Min(1)
        @field:Max(1000)
        val maxCountPerExecution: Int,
    )

}