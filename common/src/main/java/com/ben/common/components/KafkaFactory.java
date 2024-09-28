package com.ben.common.components;

import com.ben.common.configs.KafkaProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Builder
@FieldDefaults(level = AccessLevel.PROTECTED)
public class KafkaFactory<T> {

    KafkaProperty kafkaProperty;

/*_________________________________________________TOPICS-CONFIG________________________________________________________*/
    public static NewTopic createTopic(String topicName, int partitions, int replicas) {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

/*_________________________________________________CONTAINER-FACTORIES________________________________________________________*/
    public ConcurrentKafkaListenerContainerFactory<String, T> createContainerFactory(
            ConsumerFactory<String, T> consumerFactory,
            int concurrency,
            String clientId) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setClientId(clientId);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

/*_________________________________________________PRODUCER-FACTORIES________________________________________________________*/
    public ProducerFactory<String, T> createProducerFactory(Object valueSerializer, Map<String, Object> additionalProperties) {
        Map<String, Object> newProperties = new HashMap<>(kafkaProperty.getProducerProperties());
        newProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        newProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        newProperties.putAll(additionalProperties);
        return new DefaultKafkaProducerFactory<>(newProperties);
    }

/*_________________________________________________CONSUMER-FACTORIES________________________________________________________*/
    public ConsumerFactory<String, T> createConsumerFactory(String groupId, Object valueDeserializer, Map<String, Object> additionalProperties) {
        Map<String, Object> newProperties = new HashMap<>(kafkaProperty.getConsumerProperties());
        newProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        newProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        newProperties.putAll(additionalProperties);
        return new DefaultKafkaConsumerFactory<>(newProperties);
    }

}
