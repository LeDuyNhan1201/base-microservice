package com.ben.mail;

import com.ben.mail.enums.VerificationType;
import com.ben.mail.exceptions.AppException;
import com.ben.mail.services.SendMailService;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

import static com.ben.mail.Constants.KAFKA_TOPIC_SEND_MAIL;
import static com.ben.mail.exceptions.AppErrorCode.SEND_MAIL_ERROR;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SendEmailConsumer {

    SendMailService sendMailService;

    @KafkaListener(
            topics = KAFKA_TOPIC_SEND_MAIL,
            groupId = "${spring.kafka.identity-consumer.group-id}",
            errorHandler = "kafkaListenerErrorHandler"
    )
    public void listenNotificationDelivery(String message) {
        String type = message.split(":")[0];
        String email = message.split(":")[1];
        String token = message.split(":")[2];
        String code = message.split(":")[3];

        log.info("Message received: {}", message);

        try {
            switch (VerificationType.valueOf(type)) {
                case VERIFY_EMAIL_BY_CODE -> sendMailService.sendMailToVerifyWithCode(email, code);

                case VERIFY_EMAIL_BY_TOKEN -> sendMailService.sendMailToVerifyWithToken(email, token);

                case RESET_PASSWORD -> sendMailService.sendMailToResetPassword(email, code);
            }
        }
        catch (MessagingException | UnsupportedEncodingException e) {
            throw new AppException(SEND_MAIL_ERROR, UNPROCESSABLE_ENTITY, "Failed to create MimeMessageHelper");
        }
    }

}
