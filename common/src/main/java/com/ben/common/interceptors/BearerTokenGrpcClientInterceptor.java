package com.ben.common.interceptors;

import com.ben.common.Constants;
import io.grpc.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.ben.common.Utils.convertToUpperHyphen;
import static com.ben.common.Constants.AUTHORIZATION_KEY;

@AllArgsConstructor
@Slf4j
public class BearerTokenGrpcClientInterceptor implements ClientInterceptor {

    protected final String microserviceName;

    protected final String bearerToken;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        String[] methodNameRaw = method.getFullMethodName().split("\\.");
        String methodName = methodNameRaw[methodNameRaw.length - 1];
        log.info("[{}] -> [{}]: {}", microserviceName, convertToUpperHyphen(methodName.split("/")[0]), methodName);

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Lấy token từ Context hoặc ThreadLocal
//                String token = (Constants.GRPC_AUTHORIZATION_CONTEXT.get() != null)
//                        ? Constants.GRPC_AUTHORIZATION_CONTEXT.get() : Constants.REST_AUTHORIZATION_CONTEXT.get();

                log.info("[{}]: Bearer Token: {}", microserviceName, bearerToken);
                // Thêm token vào header của outgoing gRPC request nếu token tồn tại
                if (bearerToken != null) headers.put(AUTHORIZATION_KEY, bearerToken);

                super.start(responseListener, headers);
            }

            @Override
            public void sendMessage(ReqT message) {
                super.sendMessage(message);
            }

        };
    }

}
