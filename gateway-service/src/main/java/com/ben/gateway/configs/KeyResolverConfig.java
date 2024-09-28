package com.ben.gateway.configs;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static com.ben.gateway.utils.Constants.MICROSERVICE_NAME;

@Configuration
@Slf4j
public class KeyResolverConfig {

    private KeyResolver ipAddressResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
    }

    private KeyResolver tokenSubjectKeyResolver() {
        return exchange -> {
            // Extract token from the Authorization header
            List<String> authHeaders = exchange.getRequest().getHeaders().get("Authorization");
            if (authHeaders == null || authHeaders.isEmpty()) {
                return Mono.empty(); // No token found, return empty Mono
            }

            String token = authHeaders.getFirst().replace("Bearer ", "");

            // Parse the subject (identity) from the token
            String subject = getSubjectFromToken(token);
            log.info("[{}]: Subject: {}", MICROSERVICE_NAME, subject);
            // Use subject as the key resolver
            return Mono.just(subject);
        };
    }

    @Bean
    public KeyResolver combinedKeyResolver() {
        return exchange -> {
            List<String> authHeaders = exchange.getRequest().getHeaders().get("Authorization");
//            if (authHeaders != null && !authHeaders.isEmpty()) {
//                // Prefer tokenSubjectKeyResolver if token is available
//                return tokenSubjectKeyResolver().resolve(exchange);
//            } else {
//                // Fallback to ipAddressResolver
//                return ipAddressResolver().resolve(exchange);
//            }
           return ipAddressResolver().resolve(exchange);
        };
    }

    private String getSubjectFromToken(String token) {
        //log.info("[{}]: Token: {}", MICROSERVICE_NAME, Jwts.parser().build().parse(token).getPayload());
        //return Jwts.parser().build().parseEncryptedClaims(token).getPayload().getSubject();
        return token;
    }

}
