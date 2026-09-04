package com.sunrisedental.filter;

import com.sunrisedental.util.SessionHelper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Session-based security filter — protects /api/* except login.
 */
@WebFilter(urlPatterns = {"/api/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath() + (req.getPathInfo() != null ? req.getPathInfo() : "");
        if ("POST".equalsIgnoreCase(req.getMethod()) && path.endsWith("/api/auth/login")) {
            chain.doFilter(request, response);
            return;
        }

        if (SessionHelper.getUser(req) == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"message\":\"Not authenticated. Please login.\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
