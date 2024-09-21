package com.ben.profile.repositories.externals;

import com.ben.grpc.identity.IdentityServiceGrpc;
import com.ben.grpc.identity.IntrospectRequest;
import com.ben.profile.dtos.responses.IntrospectResponse;
import com.ben.profile.exceptions.AppErrorCode;
import com.ben.profile.exceptions.AppException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.ben.profile.utils.Constants.MICROSERVICE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityServiceGrpcClient {

    private final IdentityServiceGrpc.IdentityServiceBlockingStub identityServiceClient;

    public IntrospectResponse introspect(String token) {
        try {
            IntrospectResponse response = new IntrospectResponse(false);
            response.setValid(
                    identityServiceClient.introspect(IntrospectRequest.newBuilder().setToken(token).build()).getValid());
            return response;

        } catch (Exception e) {
            log.info("[{}]: Error occurred while introspecting token: {}", MICROSERVICE_NAME, e.getMessage());
            throw new AppException(AppErrorCode.INTROSPECT_FAILED, Status.UNAUTHENTICATED, "Token introspection failed");
        }
    }

}
