package io.scheduler.comparison.jobrunr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("io.scheduler.comparison.jobrunr.config.properties")
class SchedulerJobrunrApplication

fun main(args: Array<String>) {
    runApplication<SchedulerJobrunrApplication>(*args)
}
