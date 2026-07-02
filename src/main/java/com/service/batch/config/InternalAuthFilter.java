package com.service.batch.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class InternalAuthFilter extends OncePerRequestFilter {
    private static final String AUTH_HEADER = "Auth-header";
    private static final String INTERNAL_AUTH_HEADER_VALUE = "second";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isExemptPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!INTERNAL_AUTH_HEADER_VALUE.equals(request.getHeader(AUTH_HEADER))) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExemptPath(String path) {
        return path.equals("/actuator")
                || path.startsWith("/actuator/")
                || path.equals("/error")
                || path.equals("/service/batch/metrics")
                || path.startsWith("/service/batch/metrics/")
                || path.equals("/service/batch/webhook")
                || path.startsWith("/service/batch/webhook/");
    }
}
