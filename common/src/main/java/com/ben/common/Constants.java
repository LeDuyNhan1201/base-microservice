package com.ben.common;

import io.grpc.Context;
import io.grpc.Metadata;

public class Constants {

    public static final Metadata.Key<String> AUTHORIZATION_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> GRPC_AUTHORIZATION_CONTEXT = Context.key("authToken");

    public static final ThreadLocal<String> REST_AUTHORIZATION_CONTEXT = new ThreadLocal<>();

}