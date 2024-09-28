package com.ben.gateway.components;

import com.ben.gateway.dtos.CommonResponse;
import com.ben.gateway.exceptions.AppErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.ben.gateway.utils.Constants.MICROSERVICE_NAME;
import static com.ben.gateway.components.Translator.getLocalizedMessage;
import static com.ben.gateway.exceptions.AppErrorCode.TOKEN_INVALID;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {

    ObjectMapper objectMapper;

    @NonFinal
    private String[] publicEndpoints = {
            "/identity/auth/sign-up",
            "/identity/auth/verify-email-by-code",
            "/identity/auth/verify-email-by-token",
            "/identity/auth/send-email-verification",
            "/identity/auth/send-forgot-password",
            "/identity/auth/forgot-password",
            "/identity/auth/reset-password",
            "/identity/auth/sign-in",
            "/identity/auth/sign-out",
            "/identity/api-docs/**",
            "/profile/api-docs/**"
    };

    List<Locale> LOCALES = List.of(
            Locale.forLanguageTag("en"),
            Locale.forLanguageTag("vi")
    );

    @Bean
    public MessageSource messageSource(
            @Value("${spring.messages.basename}") String basename,
            @Value("${spring.messages.encoding}") String encoding,
            @Value("${spring.messages.default-locale}") String defaultLocale,
            @Value("${spring.messages.cache-duration}") int cacheSeconds
    ) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(basename);
        messageSource.setDefaultEncoding(encoding);
        messageSource.setDefaultLocale(Locale.of(defaultLocale));
        messageSource.setCacheSeconds(cacheSeconds);
        return messageSource;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("[{}]: Enter authentication filter....", MICROSERVICE_NAME);

        String headerLang = exchange.getRequest().getHeaders().getFirst("Accept-Language");
        log.info("Accept-Language: {}", headerLang);
        Locale locale = StringUtils.hasLength(headerLang)
                ? Locale.lookup(Locale.LanguageRange.parse(headerLang), LOCALES)
                : Locale.getDefault();

        LocaleContextHolder.setLocale(locale);

        log.info("Resolved Locale: {}", LocaleContextHolder.getLocale());

        if (isPublicEndpoint(exchange.getRequest()))
            return chain.filter(exchange);

        // Get token from authorization header
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader))
            return unauthenticated(exchange.getResponse());

        String token = authHeader.getFirst().replace("Bearer ", "");
        log.info("[{}]: Token: {}", MICROSERVICE_NAME, token);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        PathMatcher pathMatcher = new AntPathMatcher();
        String requestPath = request.getURI().getPath();

        return Arrays.stream(publicEndpoints)
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    Mono<Void> unauthenticated(ServerHttpResponse response) {
        AppErrorCode errorCode = TOKEN_INVALID;
        CommonResponse<?, ?> apiResponse = CommonResponse.builder()
                .errorCode(errorCode.getCode())
                .message(getLocalizedMessage(errorCode.getMessage()))
                .build();

        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

}