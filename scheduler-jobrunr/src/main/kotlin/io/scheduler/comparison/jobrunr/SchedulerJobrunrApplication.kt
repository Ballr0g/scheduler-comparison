package io.scheduler.comparison.jobrunr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SchedulerJobrunrApplication

fun main(args: Array<String>) {
    runApplication<SchedulerJobrunrApplication>(*args)
}
