package com.ben.common.configs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.config.TopicBuilder;

import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaProperty {

    String bootstrapServers;

    String keystoreLocation;

    String keystorePassword;

    String truststoreLocation;

    String truststorePassword;

    String keyPassword;

    @Getter
    @NonFinal
    protected Map<String, Object> producerProperties;

    @Getter
    @NonFinal
    protected Map<String, Object> consumerProperties;


    public KafkaProperty(String bootstrapServers, String keystoreLocation, String keystorePassword, String truststoreLocation, String truststorePassword, String keyPassword) {
        this.bootstrapServers = bootstrapServers;
        this.keystoreLocation = keystoreLocation;
        this.keystorePassword = keystorePassword;
        this.truststoreLocation = truststoreLocation;
        this.truststorePassword = truststorePassword;
        this.keyPassword = keyPassword;
        setConsumerProperties();
        setProducerProperties();
    }

/*_________________________________________________TOPICS-CONFIG________________________________________________________*/
    protected NewTopic createTopic(String topicName, int partitions, int replicas) {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

/*_________________________________________________COMMON-PROPERTIES________________________________________________________*/
    private void setProducerProperties() {
        producerProperties = new HashMap<>(commonConfigs());
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    }

    private void setConsumerProperties() {
        consumerProperties = new HashMap<>(commonConfigs());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProperties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    }

    private Map<String, Object> commonConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put("ssl.keystore.location", keystoreLocation);
        props.put("ssl.keystore.password", keystorePassword);
        props.put("ssl.truststore.location", truststoreLocation);
        props.put("ssl.truststore.password", truststorePassword);
        props.put("ssl.key.password", keyPassword);
        return props;
    }

//    @Bean
//    public ProducerFactory<Object, Object> producerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
//        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
//        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
//        props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
//                "%s required username=\"%s\" " + "password=\"%s\";", PlainLoginModule.class.getName(), "ben1201", "Ben1201#"
//        ));
//        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "identity-certs\\identity.keystore.jks");
//        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "Ben1201#");
//        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "identity-certs\\identity.truststore.jks");
//        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "Ben1201#");
//        return new DefaultKafkaProducerFactory<>(props);
//    }

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

}
