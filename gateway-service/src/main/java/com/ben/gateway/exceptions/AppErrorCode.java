package com.ben.gateway.exceptions;

import lombok.Getter;

@Getter
public enum AppErrorCode {

    TOKEN_INVALID("auth/token-invalid", "token_invalid"),
    FALLBACK_ERROR("fallback/error", "fallback_error"),
    ;

    AppErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private final String code;
    private final String message;

}