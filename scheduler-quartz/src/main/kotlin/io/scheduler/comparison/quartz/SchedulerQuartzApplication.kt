package io.scheduler.comparison.quartz

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("io.scheduler.comparison.quartz.config.properties")
class SchedulerQuartzApplication

fun main(args: Array<String>) {
    runApplication<SchedulerQuartzApplication>(*args)
}
