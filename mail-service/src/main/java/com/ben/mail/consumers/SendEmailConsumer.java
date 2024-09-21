package com.ben.mail.consumers;

import com.ben.mail.enums.VerificationType;
import com.ben.mail.exceptions.AppException;
import com.ben.mail.services.SendMailService;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import static com.ben.mail.utils.Constants.KAFKA_TOPIC_SEND_MAIL;
import static com.ben.mail.utils.Constants.MICROSERVICE_NAME;
import static com.ben.mail.exceptions.AppErrorCode.SEND_MAIL_ERROR;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SendEmailConsumer {

    SendMailService sendMailService;

    LocaleResolver localeResolver;

    @KafkaListener(
            topics = KAFKA_TOPIC_SEND_MAIL,
            groupId = "${spring.kafka.identity-consumer.group-id}",
            containerFactory = "sendMailContainerFactory",
            errorHandler = "kafkaListenerErrorHandler")
    @RetryableTopic(
            attempts = "5",
            backoff = @Backoff(delay = 1000, multiplier = 1.0, maxDelay = 5000),
            topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
            retryTopicSuffix = "-retrytopic",
            dltTopicSuffix = "-dlttopic",
            include = { AppException.class }
    )
    public void listenNotificationDelivery(String message, Acknowledgment acknowledgment) {
        String type = message.split(":")[0];
        String email = message.split(":")[1];
        String token = message.split(":")[2];
        String code = message.split(":")[3];
        String languageCode = message.split(":")[4];

        LocaleContextHolder.setLocale(Locale.of(languageCode));
        log.info("[{}]: Message received: {}", MICROSERVICE_NAME, message);

        try {
            switch (VerificationType.valueOf(type)) {
                case VERIFY_EMAIL_BY_CODE -> sendMailService.sendMailToVerifyWithCode(email, code);

                case VERIFY_EMAIL_BY_TOKEN -> sendMailService.sendMailToVerifyWithToken(email, token);

                case RESET_PASSWORD -> sendMailService.sendMailToResetPassword(email, code);
            }
            acknowledgment.acknowledge();
        }
        catch (MessagingException | UnsupportedEncodingException e) {
            throw new AppException(SEND_MAIL_ERROR, UNPROCESSABLE_ENTITY, "Failed to create MimeMessageHelper");
        }
    }

    @DltHandler
    public void dtl(String message, @Header(RECEIVED_TOPIC) String topic) {
        log.info("[{}]: DTL TOPIC message : {}, topic name : {}", MICROSERVICE_NAME, message, topic);
    }

}
