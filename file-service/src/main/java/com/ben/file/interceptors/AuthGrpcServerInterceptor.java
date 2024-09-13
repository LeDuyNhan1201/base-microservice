package com.ben.file.interceptors;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import javax.crypto.spec.SecretKeySpec;

import static com.ben.file.Constants.*;
import static io.grpc.Status.UNAUTHENTICATED;

@GrpcGlobalServerInterceptor
@Slf4j
public class AuthGrpcServerInterceptor implements ServerInterceptor {

    @Value("${jwt.accessSignerKey}")
    private String ACCESS_SIGNER_KEY;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    private final String[] PUBLIC_METHODS = new String[] {
//            "FileService/uploadFile"
    };

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        // Get the method name from the ServerCall object
        String[] methodNameRaw = call.getMethodDescriptor().getFullMethodName().split("\\.");
        String methodName = methodNameRaw[methodNameRaw.length - 1];

        // Check if the method is public
        for (String method : PUBLIC_METHODS) if (methodName.contains(method)) return next.startCall(call, headers);

        // Extract the token from the headers
        String token = headers.get(AUTHORIZATION_KEY);

        if (token == null) {
            log.error("Token missing");
            call.close(UNAUTHENTICATED.withDescription("Token missing"), headers);
            return new ServerCall.Listener<>() {};

        } else {
            log.info("Token: {}", token.substring(7));
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    ACCESS_SIGNER_KEY.getBytes(),
                    ACCESS_TOKEN_SIGNATURE_ALGORITHM.getName());

            if (nimbusJwtDecoder == null) {
                nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                        .macAlgorithm(MacAlgorithm.from(ACCESS_TOKEN_SIGNATURE_ALGORITHM.getName()))
                        .build();
            }

            JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(nimbusJwtDecoder.decode(token.substring(7)));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // Store the token in gRPC Context for later use in request lifecycle
        Context ctx = Context.current().withValue(AUTHORIZATION_CONTEXT, token);

        return Contexts.interceptCall(ctx, call, headers, next);
    }

}
