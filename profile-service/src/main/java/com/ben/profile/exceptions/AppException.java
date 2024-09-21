package com.ben.profile.exceptions;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppException extends RuntimeException {

    public AppException(AppErrorCode appErrorCode, HttpStatus httpStatus, String reason, Object... moreInfo) {
        super(appErrorCode.getMessage());
        this.reason = reason;
        this.appErrorCode = appErrorCode;
        this.httpStatus = httpStatus;
        this.moreInfo = moreInfo;
    }

    public AppException(AppErrorCode appErrorCode, Status grpcStatus, String reason, Object... moreInfo) {
        super(appErrorCode.getMessage());
        this.reason = reason;
        this.appErrorCode = appErrorCode;
        this.grpcStatus = grpcStatus;
        this.moreInfo = moreInfo;
    }

    final AppErrorCode appErrorCode;
    final String reason;
    HttpStatus httpStatus;
    Status grpcStatus;
    final Object[] moreInfo;

}
