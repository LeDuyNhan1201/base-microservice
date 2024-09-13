package com.ben.identity.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppException extends RuntimeException {

    public AppException(AppErrorCode appErrorCode, HttpStatus httpStatus, String reason) {
        super(appErrorCode.getMessage());
        this.appErrorCode = appErrorCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
    }

    final AppErrorCode appErrorCode;
    final HttpStatus httpStatus;
    final String reason;
    @Setter
    Object[] moreInfo;

}
