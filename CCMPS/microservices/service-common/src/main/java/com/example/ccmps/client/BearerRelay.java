package com.example.ccmps.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class BearerRelay implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        if (template.headers().containsKey("Authorization")) {
            return;
        }
        if (SecurityContextHolder.getContext().getAuthentication()
                instanceof JwtAuthenticationToken authentication) {
            template.header("Authorization", "Bearer " + authentication.getToken().getTokenValue());
        } else if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes) {
            // Feign circuit-breaker workers propagate request attributes, not the security context.
            String bearer = attributes.getRequest().getHeader("Authorization");
            if (bearer != null && bearer.startsWith("Bearer ")) {
                template.header("Authorization", bearer);
            }
        }
    }
}
