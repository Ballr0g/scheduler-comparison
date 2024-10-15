package io.scheduler.comparison.dbscheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DbSchedulerApplication

fun main(args: Array<String>) {
	runApplication<DbSchedulerApplication>(*args)
}
