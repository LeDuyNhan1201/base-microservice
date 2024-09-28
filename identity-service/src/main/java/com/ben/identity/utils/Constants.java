package com.ben.identity.utils;

import com.nimbusds.jose.JWSAlgorithm;
import io.grpc.Context;
import io.grpc.Metadata;

import static com.nimbusds.jose.JWSAlgorithm.HS384;
import static com.nimbusds.jose.JWSAlgorithm.HS512;

public class Constants {

    public static String MICROSERVICE_NAME = "IDENTITY-SERVICE";

    public static final Metadata.Key<String> AUTHORIZATION_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> GRPC_AUTHORIZATION_CONTEXT = Context.key("authToken");

    public static final ThreadLocal<String> REST_AUTHORIZATION_CONTEXT = new ThreadLocal<>();

    public static final JWSAlgorithm ACCESS_TOKEN_SIGNATURE_ALGORITHM = HS512;

    public static final JWSAlgorithm REFRESH_TOKEN_SIGNATURE_ALGORITHM = HS384;

    public static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static final String KAFKA_TOPIC_CREATE_PROFILE = "CREATE_PROFILE";

    public static final String KAFKA_TOPIC_SEND_MAIL = "SEND_MAIL";

}