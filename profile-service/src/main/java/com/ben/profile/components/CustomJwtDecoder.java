package com.ben.profile.components;

import com.ben.profile.exceptions.AppException;
import com.ben.profile.repositories.externals.IdentityServiceGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

import static com.ben.profile.exceptions.AppErrorCode.INTROSPECT_FAILED;
import static com.ben.profile.utils.Constants.ACCESS_TOKEN_SIGNATURE_ALGORITHM;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.accessSignerKey}")
    private String ACCESS_SIGNER_KEY;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    private final IdentityServiceGrpcClient identityServiceGrpcClient;

    @Override
    public Jwt decode(String token) {
        try {
            if (!identityServiceGrpcClient.introspect(token).isValid())
                throw new AppException(INTROSPECT_FAILED, UNAUTHORIZED, "Introspection failed");

        } catch (AppException e) {
            throw new JwtException(e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    ACCESS_SIGNER_KEY.getBytes(),
                    ACCESS_TOKEN_SIGNATURE_ALGORITHM.getName());

            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.from(ACCESS_TOKEN_SIGNATURE_ALGORITHM.getName()))
                    .build();
        }
        return nimbusJwtDecoder.decode(token);
    }

}