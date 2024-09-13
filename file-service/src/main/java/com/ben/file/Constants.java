package com.ben.file;

import com.ben.grpc.file.FileMetadata;
import com.nimbusds.jose.JWSAlgorithm;
import io.grpc.Context;
import io.grpc.Metadata;

import static com.nimbusds.jose.JWSAlgorithm.HS512;

public class Constants {

    public static String MICROSERVICE_NAME = "FILE-SERVICE";

    public static final  Metadata.Key<byte[]> FILE_METADATA_KEY = Metadata.Key.of("file-meta-bin", Metadata.BINARY_BYTE_MARSHALLER);

    public static final Context.Key<FileMetadata> FILE_METADATA_CONTEXT = Context.key("file-meta");

    public static final Metadata.Key<String> AUTHORIZATION_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> AUTHORIZATION_CONTEXT = Context.key("authToken");

    public static final JWSAlgorithm ACCESS_TOKEN_SIGNATURE_ALGORITHM = HS512;

}