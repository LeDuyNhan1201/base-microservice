package com.ben.profile.exceptions;

import com.ben.profile.dtos.responses.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.ben.profile.utils.Constants.MICROSERVICE_NAME;
import static com.ben.profile.components.Translator.getLocalizedMessage;
import static com.ben.profile.exceptions.AppErrorCode.VALIDATION_ERROR;
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

    @ExceptionHandler(value = { AuthorizationDeniedException.class, HttpMediaTypeNotAcceptableException.class })
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