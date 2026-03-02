package com.eflow.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Ngăn browser cache mọi API response.
 * Áp dụng cho tất cả request vào /api/**
 */
@Component
public class NoCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        chain.doFilter(request, response);
    }
}
