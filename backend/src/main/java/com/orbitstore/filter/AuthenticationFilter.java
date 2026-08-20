package com.orbitstore.filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthenticationFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            sendUnauthorized(
                    httpResponse,
                    "Authentication required.");
            return;
        }

        Object userId = session.getAttribute("userId");

        Object userRole = session.getAttribute("userRole");

        Object userStatus = session.getAttribute("userStatus");

        if (userId == null ||
                userRole == null ||
                userStatus == null) {

            sendUnauthorized(
                    httpResponse,
                    "Authentication required.");
            return;
        }

        if (!"ACTIVE".equals(userStatus.toString())) {
            sendUnauthorized(
                    httpResponse,
                    "Account is not active.");
            return;
        }

        chain.doFilter(
                request,
                response);
    }

    private void sendUnauthorized(
            HttpServletResponse response,
            String message) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorResponse = new LinkedHashMap<>();

        errorResponse.put(
                "success",
                false);

        errorResponse.put(
                "message",
                message);

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse);
    }
}