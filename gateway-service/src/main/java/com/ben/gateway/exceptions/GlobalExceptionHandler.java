package com.ben.gateway.exceptions;

import com.ben.gateway.dtos.CommonResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import static com.ben.gateway.components.Translator.getLocalizedMessage;
import static com.ben.gateway.utils.Constants.MICROSERVICE_NAME;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class GlobalExceptionHandler {

    ObjectMapper objectMapper;

    @ExceptionHandler(value = RuntimeException.class)
    Mono<Void> handlingRuntimeException(RuntimeException exception, ServerHttpResponse response) {
        log.error("Exception: ", exception);
        CommonResponse<?, ?> apiResponse = CommonResponse.builder()
                .message(getLocalizedMessage("uncategorized"))
                .build();
        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(BAD_REQUEST);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @ExceptionHandler(value = NoSuchMessageException.class)
    Mono<Void> handlingNoSuchMessageException(NoSuchMessageException exception, ServerHttpResponse response) {
        errorLogging(exception.getMessage(), exception);
        CommonResponse<?, ?> apiResponse = CommonResponse.builder()
                .message(exception.getMessage())
                .build();
        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(BAD_REQUEST);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @ExceptionHandler(value = AppException.class)
    Mono<Void> handlingAppException(AppException exception, ServerHttpResponse response) {
        errorLogging(exception.getReason(), exception);
        AppErrorCode errorCode = exception.getAppErrorCode();

        CommonResponse<?, ?> apiResponse = CommonResponse.builder()
                .errorCode(errorCode.getCode())
                .message((exception.getMoreInfo() != null)
                        ? getLocalizedMessage(errorCode.getMessage(), exception.getMoreInfo())
                        : getLocalizedMessage(errorCode.getMessage()))
                .build();

        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(exception.getHttpStatus());
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private void errorLogging(String reason, Exception exception) {
        log.error("[{}]: Reason: {} | class: {} | line: {}", MICROSERVICE_NAME,
                reason, exception.getClass(), exception.getStackTrace()[0].getLineNumber());
    }

}