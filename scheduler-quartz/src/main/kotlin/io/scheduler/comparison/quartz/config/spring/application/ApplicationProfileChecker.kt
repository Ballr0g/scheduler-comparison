package io.scheduler.comparison.quartz.config.spring.application

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Component

@Component
class ApplicationProfileChecker(
    private val environment: Environment
) : ApplicationListener<ContextRefreshedEvent> {

    private companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        checkJobStorageProfiles()
    }

    fun checkJobStorageProfiles() {
        log.info { "Active profiles$$ ${environment.activeProfiles.toList()}" }
        check (environment.acceptsProfiles(Profiles.of("!(persistent & ram)"))) {
            throw IllegalStateException("Both \"ram\" and \"persistent\" profiles cannot be present at the same time, "
                    + "choose \"ram\" for RAM Quartz JobStore or \"persistent\" for JDBC JobStore.")
        }

        check (environment.acceptsProfiles(Profiles.of("persistent | ram"))) {
            throw IllegalStateException("At least one profile (either \"ram\" or \"persistent\") must be present "
                    + "because this defines the behavior of Quartz (RAM JobStore or JDBC JobStore).")
        }
    }

}
