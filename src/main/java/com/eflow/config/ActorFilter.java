package com.eflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter đọc header {@code X-Username} từ mọi request,
 * tra cứu role và department từ {@link UserRegistry},
 * rồi lưu vào {@link RequestContext} để các controller/service có thể kiểm tra quyền.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class ActorFilter extends OncePerRequestFilter {

    private final UserRegistry userRegistry;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String username = request.getHeader("X-Username");
        RequestContext.setActor(username);

        if (username != null && !username.isBlank()) {
            RequestContext.setRole(userRegistry.getRole(username));
            RequestContext.setManagerDepartment(userRegistry.getDepartment(username));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }
}
