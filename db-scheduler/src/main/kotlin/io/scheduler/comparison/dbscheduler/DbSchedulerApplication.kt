package io.scheduler.comparison.dbscheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("io.scheduler.comparison.dbscheduler.config.properties")
class DbSchedulerApplication

fun main(args: Array<String>) {
	runApplication<DbSchedulerApplication>(*args)
}
