package com.ben.profile.interceptors;

import com.ben.profile.utils.Constants;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;

import static com.ben.common.Utils.convertToUpperHyphen;
import static com.ben.profile.utils.Constants.AUTHORIZATION_KEY;
import static com.ben.profile.utils.Constants.MICROSERVICE_NAME;

@GrpcGlobalClientInterceptor
@Slf4j
public class GrpcClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        String[] methodNameRaw = method.getFullMethodName().split("\\.");
        String methodName = methodNameRaw[methodNameRaw.length - 1];
        log.info("[{}] -> [{}]: {}", MICROSERVICE_NAME, convertToUpperHyphen(methodName.split("/")[0]), methodName);

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String token = (Constants.GRPC_AUTHORIZATION_CONTEXT.get() != null)
                        ? Constants.GRPC_AUTHORIZATION_CONTEXT.get() : Constants.REST_AUTHORIZATION_CONTEXT.get();

                log.info("[{}]: Bearer Token: {}", MICROSERVICE_NAME, token);
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
