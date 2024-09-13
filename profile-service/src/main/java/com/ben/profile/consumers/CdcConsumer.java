package com.ben.profile.consumers;

import com.ben.profile.dtos.debezium.SchemaChanges;
import com.ben.profile.dtos.debezium.UserProfileDataChanges;
import com.ben.profile.exceptions.AppException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import static com.ben.profile.Constants.KAFKA_CDC_GROUP;
import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CdcConsumer {

    @KafkaListener(
            topics = "profile_service.profile_service_db.user_profiles",
            groupId = KAFKA_CDC_GROUP,
            containerFactory = "CDCContainerFactory",
            errorHandler = "kafkaListenerErrorHandler")
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 1.0, maxDelay = 5000),
            topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
            retryTopicSuffix = "-retrytopic",
            dltTopicSuffix = "-dlttopic",
            include = { AppException.class }
    )
    public void listenUserProfileChangeData(String message, Acknowledgment acknowledgment) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            SchemaChanges<UserProfileDataChanges> schemaChanges = objectMapper.readValue(message,
                    new TypeReference<>() {
                    });

            // Accessing the data
            UserProfileDataChanges before = schemaChanges.getPayload().getBefore();
            UserProfileDataChanges after = schemaChanges.getPayload().getAfter();

            log.info("Message received: {}", after.getUser_id());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
        //acknowledgment.acknowledge();
    }

    @DltHandler
    public void dtl(String message, @Header(RECEIVED_TOPIC) String topic) {
        log.info("DTL TOPIC message : {}, topic name : {}", message, topic);
    }

}
