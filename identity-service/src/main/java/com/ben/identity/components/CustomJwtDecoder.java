package com.ben.identity.components;

import com.ben.identity.exceptions.AppException;
import com.ben.identity.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

import static com.ben.identity.Constants.ACCESS_TOKEN_SIGNATURE_ALGORITHM;
import static com.ben.identity.exceptions.AppErrorCode.INVALID_TOKEN;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.accessSignerKey}")
    private String ACCESS_SIGNER_KEY;

    private final AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) {
        try {
            if (!authenticationService.introspect(token)) throw new AppException(INVALID_TOKEN, BAD_GATEWAY, "Introspection failed");

        } catch (JOSEException | ParseException e) {
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