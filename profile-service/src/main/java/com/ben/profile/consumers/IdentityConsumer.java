package com.ben.profile.consumers;

import com.ben.grpc.profile.CreateProfileRequest;
import com.ben.profile.entities.UserProfile;
import com.ben.profile.exceptions.AppException;
import com.ben.profile.services.UserProfileService;
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

import static com.ben.profile.Constants.KAFKA_TOPIC_CREATE_PROFILE;
import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IdentityConsumer {

    UserProfileService userProfileService;

    @KafkaListener(
            topics = KAFKA_TOPIC_CREATE_PROFILE,
            groupId = "${spring.kafka.identity-consumer.group-id}",
            containerFactory = "createProfileContainerFactory",
//            properties = "specific.protobuf.value.type: com.ben.grpc.profile_service.CreateProfileRequest",
//            concurrency = "1",
            errorHandler = "kafkaListenerErrorHandler")
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 1.0, maxDelay = 5000),
            topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
            retryTopicSuffix = "-retrytopic",
            dltTopicSuffix = "-dlttopic",
            include = { AppException.class }
    )
    public void listenCreateProfile(CreateProfileRequest message, Acknowledgment acknowledgment) {
        log.info("Message received: {}", message);
        userProfileService.createUser(UserProfile.builder()
                .firstName(message.getFirstName())
                .lastName(message.getLastName())
                .userId(message.getUserId())
                .build());

        acknowledgment.acknowledge();
    }

    @DltHandler
    public void dtl(String message, @Header(RECEIVED_TOPIC) String topic) {
        log.info("DTL TOPIC message : {}, topic name : {}", message, topic);
    }

}
