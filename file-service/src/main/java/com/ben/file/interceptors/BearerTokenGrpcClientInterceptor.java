package com.ben.file.interceptors;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;

import static com.ben.common.Utils.convertToUpperHyphen;
import static com.ben.file.Constants.*;

@GrpcGlobalClientInterceptor
@Slf4j
public class BearerTokenGrpcClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        String[] methodNameRaw = method.getFullMethodName().split("\\.");
        String methodName = methodNameRaw[methodNameRaw.length - 1];
        log.info("[{}] -> [{}]: {}", MICROSERVICE_NAME, convertToUpperHyphen(methodName.split("/")[0]), methodName);

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Lấy token từ Context hoặc ThreadLocal
                String token = AUTHORIZATION_CONTEXT.get();

                // Thêm token vào header của outgoing gRPC request nếu token tồn tại
                if (token != null) headers.put(AUTHORIZATION_KEY, token);

                super.start(responseListener, headers);
            }

            @Override
            public void sendMessage(ReqT message) {
                super.sendMessage(message);
            }

        };
    }

}
