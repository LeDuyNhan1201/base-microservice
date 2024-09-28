package com.ben.identity.configs;

import com.ben.common.components.KafkaFactory;
import com.ben.common.configs.KafkaProperty;
import com.ben.grpc.profile.CreateProfileRequest;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;

import java.util.Map;

import static com.ben.identity.utils.Constants.*;

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

/*_________________________________________________TOPICS-CONFIG________________________________________________________*/
    @Bean
    public NewTopic createProfileTopic() {
        return createTopic(KAFKA_TOPIC_CREATE_PROFILE, 3, 1);
    }

    @Bean
    public NewTopic sendMailTopic() {
        return createTopic(KAFKA_TOPIC_SEND_MAIL, 3, 1);
    }

/*_________________________________________________PRODUCER-FACTORIES________________________________________________________*/
    @Bean
    public ProducerFactory<String, CreateProfileRequest> createProfileProducer() {
        return KafkaFactory.<CreateProfileRequest>builder()
                .kafkaProperty(this)
                .build()
                .createProducerFactory(KafkaProtobufSerializer.class,
                        Map.of(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL));
    }

    @Bean
    public ProducerFactory<String, String> sendMailProducer() {
        return KafkaFactory.<String>builder()
                .kafkaProperty(this)
                .build()
                .createProducerFactory(StringSerializer.class,
                        Map.of(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL));
    }

/*_________________________________________________KAFKA-TEMPLATES________________________________________________________*/
    @Bean
    public KafkaTemplate<String, CreateProfileRequest> createProfileRequestKafkaTemplate() {
        return new KafkaTemplate<>(createProfileProducer());
    }

    @Bean
    public KafkaTemplate<String, String> sendMailKafkaTemplate() {
        return new KafkaTemplate<>(sendMailProducer());
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
