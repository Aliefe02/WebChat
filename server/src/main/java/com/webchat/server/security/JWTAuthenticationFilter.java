package com.webchat.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;  // JWTUtil will be injected here

    // Constructor injection for JWTUtil
    public JWTAuthenticationFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // Skip token processing for login and register endpoints
        if (uri.equals("/user/login") || uri.equals("/user/register")) {
            chain.doFilter(request, response);  // Proceed without checking the token
            return;
        }

        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        String userId = null;

        // Check if the token exists and starts with "Bearer"
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7); // Remove "Bearer " prefix

            try {
                // Extract the user ID from the token
                userId = jwtUtil.extractUserId(jwtToken).toString();
            } catch (Exception e) {
                logger.error("Invalid JWT token: " + e.getMessage());
            }
        }

        // If userId is found, set authentication in the SecurityContext
        if (userId != null) {
            CustomAuthenticationToken authentication = new CustomAuthenticationToken(UUID.fromString(userId));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Proceed with the request
        chain.doFilter(request, response);
    }
}

