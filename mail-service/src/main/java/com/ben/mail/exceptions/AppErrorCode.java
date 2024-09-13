package com.ben.mail.exceptions;

import lombok.Getter;

@Getter
public enum AppErrorCode {

    SEND_MAIL_ERROR("mail/send-mail-error", "send_mail_error"),
    ;

    AppErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private final String code;
    private final String message;

}