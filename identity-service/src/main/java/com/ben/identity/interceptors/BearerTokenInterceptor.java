package com.ben.identity.interceptors;

import com.ben.identity.utils.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.ben.identity.utils.Constants.MICROSERVICE_NAME;

@Component
@Slf4j
public class BearerTokenInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            Constants.REST_AUTHORIZATION_CONTEXT.set(authorizationHeader);
            log.info("[{}] Token: {}", MICROSERVICE_NAME, Constants.REST_AUTHORIZATION_CONTEXT.get());
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        // Clean up ThreadLocal after request is complete
        //log.info(ContextKeys.AUTH_TOKEN.get());
        Constants.REST_AUTHORIZATION_CONTEXT.remove();
    }

}