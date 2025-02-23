package io.scheduler.comparison.dbscheduler.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer


@Configuration(proxyBeanMethods = false)
class KafkaConfig(
    val kafkaProperties: KafkaProperties,
) {

    @Bean
    fun producerFactory(objectMapper: ObjectMapper): ProducerFactory<String, Any> {
        val kafkaProperties = mutableMapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.producer.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
        )

        val jsonSerializer = JsonSerializer<Any>(objectMapper)
        return DefaultKafkaProducerFactory(kafkaProperties, StringSerializer(), jsonSerializer)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any>
        = KafkaTemplate(producerFactory)

}