package com.ben.identity.interceptors;

import com.ben.identity.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class BearerTokenInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX))
            // Save the token in gRPC Context or a ThreadLocal for later use in gRPC client calls
            Constants.REST_AUTHORIZATION_CONTEXT.set(authorizationHeader);

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