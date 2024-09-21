package com.ben.identity.exceptions;

import com.ben.identity.dtos.responses.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.ben.identity.utils.Constants.MICROSERVICE_NAME;
import static com.ben.identity.components.Translator.getLocalizedMessage;
import static com.ben.identity.exceptions.AppErrorCode.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<?> handlingRuntimeException(RuntimeException exception) {
        log.error("Exception: ", exception);
        return ResponseEntity.status(BAD_REQUEST).body(
                CommonResponse.builder().message(getLocalizedMessage("uncategorized")));
    }

    @ExceptionHandler(value = NoSuchMessageException.class)
    ResponseEntity<?> handlingNoSuchMessageException(NoSuchMessageException exception) {
        errorLogging(exception.getMessage(), exception);
        return ResponseEntity.status(BAD_REQUEST).body(CommonResponse.builder().message(exception.getMessage()));
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    ResponseEntity<?> handlingAuthorizationDeniedException(AuthorizationDeniedException exception) {
        errorLogging(exception.getMessage(), exception);
        return ResponseEntity.status(UNAUTHORIZED).body(
                CommonResponse.builder().message(getLocalizedMessage("not_have_permission")));
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<?> handlingAppException(AppException exception) {
        errorLogging(exception.getReason(), exception);
        AppErrorCode errorCode = exception.getAppErrorCode();
        return ResponseEntity.status(exception.getHttpStatus()).body(CommonResponse.builder()
                .errorCode(errorCode.getCode())
                .message((exception.getMoreInfo() != null)
                        ? getLocalizedMessage(errorCode.getMessage(), exception.getMoreInfo())
                        : getLocalizedMessage(errorCode.getMessage()))
                .errors(switch (errorCode) {
                    case VALIDATION_ERROR -> new HashMap<>(Map.of(
                            "email", getLocalizedMessage(VALIDATION_ERROR.getMessage()),
                            "password", getLocalizedMessage(VALIDATION_ERROR.getMessage())));

                    case EXPIRED_PASSWORD ->
                            new HashMap<>(Map.of("password", getLocalizedMessage(EXPIRED_PASSWORD.getMessage())));

                    case TOKEN_INVALID ->
                            new HashMap<>(Map.of("token", getLocalizedMessage(TOKEN_INVALID.getMessage())));

                    case WRONG_PASSWORD ->
                            new HashMap<>(Map.of("password", getLocalizedMessage(WRONG_PASSWORD.getMessage())));

                    case PASSWORD_MIS_MATCH ->
                            new HashMap<>(Map.of("password", getLocalizedMessage(PASSWORD_MIS_MATCH.getMessage())));

                    case EMAIL_ALREADY_IN_USE ->
                            new HashMap<>(Map.of("email", getLocalizedMessage(EMAIL_ALREADY_IN_USE.getMessage())));

                    case WEAK_PASSWORD ->
                            new HashMap<>(Map.of("password", getLocalizedMessage(WEAK_PASSWORD.getMessage())));

                    case INVALID_EMAIL ->
                            new HashMap<>(Map.of("email", getLocalizedMessage(INVALID_EMAIL.getMessage())));

                    case TERMS_NOT_ACCEPTED ->
                            new HashMap<>(Map.of("termsAccepted", getLocalizedMessage(TERMS_NOT_ACCEPTED.getMessage())));

                    case CODE_INVALID ->
                            new HashMap<>(Map.of("code", getLocalizedMessage(CODE_INVALID.getMessage())));

                    default -> null;
                })
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidExceptions(MethodArgumentNotValidException exception) {
        errorLogging(exception.getMessage(), exception);
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors()
                .forEach((error) -> {
                    String field = ((FieldError) error).getField();
                    errors.put(field, getLocalizedMessage(error.getDefaultMessage()));
                });

        return ResponseEntity.status(BAD_REQUEST).body(
                CommonResponse.builder()
                        .errorCode(VALIDATION_ERROR.getCode())
                        .message(getLocalizedMessage(VALIDATION_ERROR.getMessage()))
                        .errors(errors)
                        .build()
        );
    }

    private void errorLogging(String reason, Exception exception) {
        log.error("[{}]: Reason: {} | class: {} | line: {}", MICROSERVICE_NAME,
                reason, exception.getClass(), exception.getStackTrace()[0].getLineNumber());
    }

}