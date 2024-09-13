package com.ben.file.interceptors;

import com.ben.grpc.file.FileMetadata;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

import static com.ben.file.Constants.FILE_METADATA_CONTEXT;
import static com.ben.file.Constants.FILE_METADATA_KEY;
import static io.grpc.Status.INTERNAL;

@GrpcGlobalServerInterceptor
@Slf4j
public class FileUploadGrpcServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> serverCallHandler) {
        FileMetadata fileMetadata = null;
        if (metadata.containsKey(FILE_METADATA_KEY)) {
            byte[] metaBytes = metadata.get(FILE_METADATA_KEY);
            try {
                fileMetadata = FileMetadata.parseFrom(metaBytes);

            } catch (InvalidProtocolBufferException e) {
                Status status = INTERNAL.withDescription("unable to create file metadata");
                serverCall.close(status, metadata);
            }

            Context context = Context.current().withValue(FILE_METADATA_CONTEXT, fileMetadata);

            return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
        }

        return new ServerCall.Listener<>() {};
    }

}