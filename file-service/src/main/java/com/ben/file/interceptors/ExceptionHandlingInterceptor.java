package com.ben.file.interceptors;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

import static com.ben.file.Constants.MICROSERVICE_NAME;

@GrpcGlobalServerInterceptor
@Slf4j
public class ExceptionHandlingInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
        
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {

            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    call.close(Status.INTERNAL
                            .withDescription(String.format("[%s]: Exception thrown by application: %s", MICROSERVICE_NAME, e.getMessage())), new Metadata());
                }
            }

            @Override
            public void onCancel() {
                super.onCancel();
                // Handle cancellation if needed
            }

            @Override
            public void onComplete() {
                super.onComplete();
                // Handle completion if needed
            }
        };
    }
}