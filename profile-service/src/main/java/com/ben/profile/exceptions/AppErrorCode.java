package com.ben.profile.exceptions;

import lombok.Getter;

@Getter
public enum AppErrorCode {
    // Validation Errors
    VALIDATION_ERROR("auth/validation-error", "validation_error"),

    // Token Errors
    TOKEN_MISSING("auth/token-missing", "token_missing"),

    //Rate Limiting Errors
    TOO_MANY_REQUESTS("auth/too-many-requests", "too_many_requests"),
    RATE_LIMIT_EXCEEDED("auth/rate-limit-exceeded", "rate_limit_exceeded"),

    DEBEZIUM_CONNECT_FAILED("profile/debezium-connect-failed", "debezium_connect_failed"),
    PROFILE_NOT_FOUND("profile/profile-not-found", "profile_not_found"),
    ;

    AppErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private final String code;
    private final String message;

}