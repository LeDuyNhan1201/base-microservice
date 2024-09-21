package com.ben.profile.exceptions;

import com.ben.grpc.utils.CustomError;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(AppException.class)
    public StatusRuntimeException handleAppException(AppException exception) {
        AppErrorCode errorCode = exception.getAppErrorCode();
        Metadata metadata = new Metadata();
        Metadata.Key<CustomError> customError = ProtoUtils.keyForProto(CustomError.getDefaultInstance());
        metadata.put(customError, CustomError.newBuilder()
                .setErrorCode(errorCode.getCode())
                .setMessage(exception.getMessage())
                .build());

        return Status.fromCode(exception.getGrpcStatus().getCode())
                .withDescription(exception.getMessage()).asRuntimeException(metadata);
    }

}
