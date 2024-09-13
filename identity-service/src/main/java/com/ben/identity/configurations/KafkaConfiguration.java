package com.ben.identity.configurations;

import com.ben.grpc.profile.CreateProfileRequest;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;

import java.util.HashMap;
import java.util.Map;
import static com.ben.identity.Constants.KAFKA_TOPIC_CREATE_PROFILE;
import static com.ben.identity.Constants.KAFKA_TOPIC_SEND_MAIL;

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

/*_________________________________________________TOPICS-CONFIG________________________________________________________*/
    @Bean
    public NewTopic createProfileTopic() {
        return TopicBuilder.name(KAFKA_TOPIC_CREATE_PROFILE)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic sendMailTopic() {
        return TopicBuilder.name(KAFKA_TOPIC_SEND_MAIL)
                .partitions(3)
                .replicas(1)
                .build();
    }

//    @Bean
//    public KafkaAdmin kafkaAdmin() {
//        Map<String, Object> configs = new HashMap<>();
//        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
//        configs.put("sasl.mechanism", "PLAIN");
//        configs.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule   required username='ben1201'   password='Ben1201#';");
//        configs.put("security.protocol", "SASL_SSL");
//        configs.put("ssl.truststore.location", "identity-certs\\identity.truststore.jks");
//        configs.put("ssl.truststore.password", "Ben1201#");
//        configs.put("ssl.keystore.location", "identity-certs\\identity.keystore.jks");
//        configs.put("ssl.keystore.password", "Ben1201#");
//        return new KafkaAdmin(configs);
//    }

/*_________________________________________________PRODUCER-FACTORIES________________________________________________________*/
    @Bean
    public ProducerFactory<String, CreateProfileRequest> createProfileProducer() {
        Map<String, Object> configProps = new HashMap<>(producerCommonConfigs());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, String> sendMailProducer() {
        Map<String, Object> configProps = new HashMap<>(producerCommonConfigs());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
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
