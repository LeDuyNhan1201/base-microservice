package com.ben.file.exceptions;

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
        this.reason = reason;
        this.appErrorCode = appErrorCode;
        this.httpStatus = httpStatus;
    }

    final AppErrorCode appErrorCode;
    final String reason;
    final HttpStatus httpStatus;
    @Setter
    Object[] moreInfo;

}
