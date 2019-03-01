package io.github.synepis.todo.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class AuthTokenFilter extends OncePerRequestFilter {
    private static final String TOKEN_HEADER = "x-auth-token";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var tokenValue = getTokenHeader(request);

        if (!StringUtils.isEmpty(tokenValue)) {
            var tokenContainer = new AuthTokenContainer(tokenValue);
            SecurityContextHolder.getContext().setAuthentication(tokenContainer);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenHeader(HttpServletRequest req) {
        return Collections.list(req.getHeaderNames()).stream()
                .filter(header -> header.equalsIgnoreCase(TOKEN_HEADER))
                .map(req::getHeader)
                .findFirst()
                .orElse(null);
    }
}