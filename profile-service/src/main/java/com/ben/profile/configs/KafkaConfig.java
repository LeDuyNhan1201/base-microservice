package com.ben.profile.configs;

import com.ben.common.components.KafkaFactory;
import com.ben.common.configs.KafkaProperty;
import com.ben.grpc.profile.CreateProfileRequest;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;

import java.util.Map;

import static com.ben.profile.utils.Constants.*;

@Configuration
@Slf4j
public class KafkaConfig extends KafkaProperty {

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String SCHEMA_REGISTRY_URL;

    public KafkaConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.properties.ssl.keystore.location}") String keystoreLocation,
            @Value("${spring.kafka.properties.ssl.keystore.password}") String keystorePassword,
            @Value("${spring.kafka.properties.ssl.truststore.location}") String truststoreLocation,
            @Value("${spring.kafka.properties.ssl.truststore.password}") String truststorePassword,
            @Value("${spring.kafka.properties.ssl.key.password}") String keyPassword) {
        super(bootstrapServers, keystoreLocation, keystorePassword, truststoreLocation, truststorePassword, keyPassword);
    }

    /*_________________________________________________CONTAINER-FACTORIES________________________________________________________*/
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreateProfileRequest> createProfileContainerFactory() {
        return KafkaFactory.<CreateProfileRequest>builder()
                .kafkaProperty(this)
                .build()
                .createContainerFactory(createProfileConsumer(), 3, MICROSERVICE_NAME);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> changeDataCaptureContainerFactory() {
        return KafkaFactory.<String>builder()
                .kafkaProperty(this)
                .build()
                .createContainerFactory(changeDataCaptureConsumerFactory(), 3, MICROSERVICE_NAME);
    }

/*_________________________________________________CONSUMER-FACTORIES________________________________________________________*/
    @Bean
    public ConsumerFactory<String, CreateProfileRequest> createProfileConsumer() {
        return KafkaFactory.<CreateProfileRequest>builder()
                .kafkaProperty(this)
                .build()
                .createConsumerFactory(KAFKA_FROM_IDENTITY_GROUP, KafkaProtobufDeserializer.class,
                        Map.of(
                                KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, CreateProfileRequest.class.getName(),
                                KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL));
    }

    @Bean
    public ConsumerFactory<String, String> changeDataCaptureConsumerFactory() {
        return KafkaFactory.<String>builder()
                .kafkaProperty(this)
                .build()
                .createConsumerFactory(KAFKA_CDC_GROUP, StringDeserializer.class,
                        Map.of(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL));
    }

/*_________________________________________________ERROR-HANDLERS________________________________________________________*/
    @Bean
    public KafkaListenerErrorHandler kafkaListenerErrorHandler() {
        return (message, exception) -> {
            // Log the error and perform custom actions
            log.error("[{}]: Error processing Kafka message: {}", MICROSERVICE_NAME, message.getPayload(), exception);
            throw new RuntimeException("Error processing Kafka message", exception);
        };
    }

}
