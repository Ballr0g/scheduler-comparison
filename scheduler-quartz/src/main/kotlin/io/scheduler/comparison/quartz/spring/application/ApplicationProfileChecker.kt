package io.scheduler.comparison.quartz.spring.application

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
        log.info { "Starting scheduler quartz for comparison with profiles: ${environment.activeProfiles.toList()}" }
        checkJobStorageProfiles()
        checkPageHandlingModeProfiles()
        checkSchedulerClusteredProfiles()
    }

    fun checkJobStorageProfiles() {
        check (environment.acceptsProfiles(Profiles.of("!(ram & persistent)"))) {
            throw IllegalStateException("Both \"ram\" and \"persistent\" profiles cannot be present at the same time, "
                    + "choose \"ram\" for RAM Quartz JobStore or \"persistent\" for JDBC JobStore.")
        }

        check (environment.acceptsProfiles(Profiles.of("ram | persistent"))) {
            throw IllegalStateException("At least one profile (either \"ram\" or \"persistent\") must be present "
                    + "because this defines the behavior of Quartz (RAM JobStore or JDBC JobStore).")
        }
    }

    fun checkPageHandlingModeProfiles() {
        check (environment.acceptsProfiles(Profiles.of("!(pagination & streaming)"))) {
            throw IllegalStateException("Both \"pagination\" and \"streaming\" profiles cannot be present at the same time, "
                    + "choose \"pagination\" for List-based entry handling or \"streaming\" for Stream-based entry handling.")
        }

        check (environment.acceptsProfiles(Profiles.of("pagination | streaming"))) {
            throw IllegalStateException("At least one profile (either \"pagination\" or \"streaming\") must be present "
                    + "because this defines the behavior of pagination (List-based or Stream-based).")
        }
    }

    fun checkSchedulerClusteredProfiles() {
        check(environment.acceptsProfiles(Profiles.of("!clustered | (clustered & persistent)"))) {
            throw IllegalStateException("\"clustered\" profile requires \"persistent\" because a persistent "
                    + "datasource is mandatory for running scheduling in cluster mode")
        }

        check (environment.acceptsProfiles(Profiles.of("!(standalone & clustered)"))) {
            throw IllegalStateException("Both \"standalone\" and \"clustered\" profiles cannot be present at the same time, "
                    + "choose \"standalone\" running the scheduling using a single replica or " +
                    " \"clustered\" for support of multiple instances.")
        }

        check (environment.acceptsProfiles(Profiles.of("standalone | clustered"))) {
            throw IllegalStateException("At least one profile (either \"standalone\" or \"clustered\") must be present "
                    + "because this defines the behavior of replicas (single replica or multiple instances in a cluster).")
        }
    }

}
