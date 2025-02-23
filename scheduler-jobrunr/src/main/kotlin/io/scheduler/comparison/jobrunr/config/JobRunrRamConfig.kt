package io.scheduler.comparison.jobrunr.config

import org.jobrunr.jobs.mappers.JobMapper
import org.jobrunr.storage.InMemoryStorageProvider
import org.jobrunr.storage.StorageProvider
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Profile("ram")
@Configuration(proxyBeanMethods = false)
class JobRunrRamConfig {

    @Bean
    fun storageProvider(jobMapper: JobMapper): StorageProvider {
        val storageProvider = InMemoryStorageProvider()
        storageProvider.setJobMapper(JobMapper(JacksonJsonMapper()))
        return storageProvider
    }

}
