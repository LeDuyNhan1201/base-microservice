package com.ben.identity.services;

import com.ben.grpc.identity.IdentityServiceGrpc;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.text.ParseException;

import static com.ben.identity.utils.Constants.MICROSERVICE_NAME;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class IdentityService extends IdentityServiceGrpc.IdentityServiceImplBase {

    private final AuthService authService;

    @Override
    public void introspect(com.ben.grpc.identity.IntrospectRequest request,
                           io.grpc.stub.StreamObserver<com.ben.grpc.identity.IntrospectResponse> responseObserver) {
        // Get the token from the request
        String token = request.getToken();

        // Initialize response builder
        com.ben.grpc.identity.IntrospectResponse.Builder responseBuilder = com.ben.grpc.identity.IntrospectResponse.newBuilder();

        try {
            // Call the introspect method to validate the token
            boolean isValid = authService.introspect(token);

            // Set the response according to the token validation result
            responseBuilder.setValid(isValid);

        } catch (JOSEException | ParseException e) {
            // Handle possible exceptions (optional: log the error)
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(String.format("[%s]: Token parsing or validation error: %s", MICROSERVICE_NAME, e.getMessage()))
                    .asRuntimeException());
            return;
        }

        // Send the response
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

}
