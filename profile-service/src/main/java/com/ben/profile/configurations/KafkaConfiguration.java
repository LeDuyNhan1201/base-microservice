package com.ben.profile.configurations;

import com.ben.grpc.profile.CreateProfileRequest;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;

import java.util.HashMap;
import java.util.Map;

import static com.ben.profile.Constants.*;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVERS;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String SCHEMA_REGISTRY_URL;

    @Value("${spring.kafka.properties.ssl.keystore.location}")
    private String KEYSTORE_LOCATION;

    @Value("${spring.kafka.properties.ssl.truststore.location}")
    private String TRUSTSTORE_LOCATION;

    @Value("${spring.kafka.properties.ssl.truststore.password}")
    private String TRUSTSTORE_PASSWORD;

    @Value("${spring.kafka.properties.ssl.keystore.password}")
    private String KEYSTORE_PASSWORD;

    @Value("${spring.kafka.properties.ssl.key.password}")
    private String KEY_PASSWORD;

/*_________________________________________________CONTAINER-FACTORIES________________________________________________________*/
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreateProfileRequest> createProfileContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CreateProfileRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createProfileConsumer());
        factory.setConcurrency(3);
        factory.getContainerProperties().setClientId(MICROSERVICE_NAME);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> CDCContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(CDCConsumer());
        factory.setConcurrency(3);
        factory.getContainerProperties().setClientId(MICROSERVICE_NAME);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

/*_________________________________________________CONSUMER-FACTORIES________________________________________________________*/
    @Bean
    public ConsumerFactory<String, CreateProfileRequest> createProfileConsumer() {
        Map<String, Object> props = new HashMap<>(consumerCommonConfigs());
        props.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, CreateProfileRequest.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_FROM_IDENTITY_GROUP);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, String> CDCConsumer() {
        Map<String, Object> props = new HashMap<>(consumerCommonConfigs());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_CDC_GROUP);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

/*_________________________________________________ERROR-HANDLERS________________________________________________________*/
    @Bean
    public KafkaListenerErrorHandler kafkaListenerErrorHandler() {
        return (message, exception) -> {
            // Log the error and perform custom actions
            log.error("Error processing Kafka message: {}", message.getPayload(), exception);
            throw new RuntimeException("Error processing Kafka message", exception);
        };
    }

/*_________________________________________________COMMON-PROPERTIES________________________________________________________*/
    private Map<String, Object> producerCommonConfigs() {
        Map<String, Object> props = new HashMap<>(commonConfigs());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    private Map<String, Object> consumerCommonConfigs() {
        Map<String, Object> props = new HashMap<>(commonConfigs());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    private Map<String, Object> commonConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put("ssl.keystore.location", KEYSTORE_LOCATION);
        props.put("ssl.keystore.password", KEYSTORE_PASSWORD);
        props.put("ssl.truststore.location", TRUSTSTORE_LOCATION);
        props.put("ssl.truststore.password", TRUSTSTORE_PASSWORD);
        props.put("ssl.key.password", KEY_PASSWORD);
        return props;
    }


}
