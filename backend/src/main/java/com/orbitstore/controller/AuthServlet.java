package com.orbitstore.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitstore.dao.UserDAO;
import com.orbitstore.dao.UserDAOImpl;
import com.orbitstore.model.User;
import com.orbitstore.service.AuthService;
import com.orbitstore.service.AuthServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthServlet extends HttpServlet {

        private final ObjectMapper objectMapper = new ObjectMapper();

        private AuthService authService;

        @Override
        public void init() throws ServletException {

                UserDAO userDAO = new UserDAOImpl();

                authService = new AuthServiceImpl(userDAO);
        }

        @Override
        protected void doPost(
                        HttpServletRequest request,
                        HttpServletResponse response) throws ServletException, IOException {

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String path = request.getPathInfo();

                if ("/register".equals(path)) {
                        handleRegister(request, response);
                        return;
                }

                if ("/login".equals(path)) {
                        handleLogin(request, response);
                        return;
                }

                if ("/logout".equals(path)) {
                        handleLogout(request, response);
                        return;
                }

                sendError(
                                response,
                                HttpServletResponse.SC_NOT_FOUND,
                                "Authentication endpoint not found.");
        }

        @Override
        protected void doGet(
                        HttpServletRequest request,
                        HttpServletResponse response) throws ServletException, IOException {

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String path = request.getPathInfo();

                if ("/me".equals(path)) {
                        handleCurrentUser(request, response);
                        return;
                }

                sendError(
                                response,
                                HttpServletResponse.SC_NOT_FOUND,
                                "Authentication endpoint not found.");
        }

        private void handleRegister(
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

                try {

                        JsonNode body = objectMapper.readTree(request.getReader());

                        String firstName = getText(body, "firstName");
                        String lastName = getText(body, "lastName");
                        String email = getText(body, "email");
                        String password = getText(body, "password");
                        String phone = getText(body, "phone");

                        User user = authService.register(
                                        firstName,
                                        lastName,
                                        email,
                                        password,
                                        phone);

                        Map<String, Object> responseBody = createUserResponse(
                                        true,
                                        "Account created successfully.",
                                        user);

                        response.setStatus(HttpServletResponse.SC_CREATED);

                        objectMapper.writeValue(
                                        response.getWriter(),
                                        responseBody);

                } catch (IllegalArgumentException e) {

                        sendError(
                                        response,
                                        HttpServletResponse.SC_BAD_REQUEST,
                                        e.getMessage());

                } catch (SQLIntegrityConstraintViolationException e) {

                        sendError(
                                        response,
                                        HttpServletResponse.SC_CONFLICT,
                                        "An account with this email already exists.");

                } catch (SQLException e) {

                        getServletContext().log(
                                        "Database error during registration.",
                                        e);

                        sendError(
                                        response,
                                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                        "Unable to create account.");
                }
        }

        private void handleLogin(
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

                try {

                        JsonNode body = objectMapper.readTree(request.getReader());

                        String email = getText(body, "email");
                        String password = getText(body, "password");

                        User user = authService.login(
                                        email,
                                        password);

                        HttpSession oldSession = request.getSession(false);

                        if (oldSession != null) {
                                oldSession.invalidate();
                        }

                        HttpSession session = request.getSession(true);

                        session.setAttribute("userId", user.getId());
                        session.setAttribute("userRole", user.getRole().name());
                        session.setAttribute("userStatus", user.getStatus().name());

                        Map<String, Object> responseBody = createUserResponse(
                                        true,
                                        "Login successful.",
                                        user);

                        response.setStatus(HttpServletResponse.SC_OK);

                        objectMapper.writeValue(
                                        response.getWriter(),
                                        responseBody);

                } catch (IllegalArgumentException e) {

                        sendError(
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        e.getMessage());

                } catch (IllegalStateException e) {

                        sendError(
                                        response,
                                        HttpServletResponse.SC_FORBIDDEN,
                                        e.getMessage());

                } catch (SQLException e) {

                        getServletContext().log(
                                        "Database error during login.",
                                        e);

                        sendError(
                                        response,
                                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                        "Unable to process login.");
                }
        }

        private void handleLogout(
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

                HttpSession session = request.getSession(false);

                if (session != null) {
                        session.invalidate();
                }

                response.setStatus(HttpServletResponse.SC_OK);

                Map<String, Object> responseBody = new LinkedHashMap<>();

                responseBody.put(
                                "success",
                                true);

                responseBody.put(
                                "message",
                                "Logout successful.");

                objectMapper.writeValue(
                                response.getWriter(),
                                responseBody);
        }

        private Map<String, Object> createUserResponse(
                        boolean success,
                        String message,
                        User user) {

                Map<String, Object> userResponse = new LinkedHashMap<>();

                userResponse.put(
                                "id",
                                user.getId());

                userResponse.put(
                                "firstName",
                                user.getFirstName());

                userResponse.put(
                                "lastName",
                                user.getLastName());

                userResponse.put(
                                "email",
                                user.getEmail());

                userResponse.put(
                                "phone",
                                user.getPhone());

                userResponse.put(
                                "role",
                                user.getRole().name());

                userResponse.put(
                                "status",
                                user.getStatus().name());

                Map<String, Object> responseBody = new LinkedHashMap<>();

                responseBody.put(
                                "success",
                                success);

                responseBody.put(
                                "message",
                                message);

                responseBody.put(
                                "user",
                                userResponse);

                return responseBody;
        }

        private String getText(
                        JsonNode body,
                        String field) {

                if (body == null || !body.has(field)) {
                        return null;
                }

                JsonNode value = body.get(field);

                if (value == null || value.isNull()) {
                        return null;
                }

                return value.asText();
        }

        private void sendError(
                        HttpServletResponse response,
                        int status,
                        String message) throws IOException {

                response.setStatus(status);

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

        private void handleCurrentUser(
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

                HttpSession session = request.getSession(false);

                if (session == null) {
                        sendError(
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentication required.");
                        return;
                }

                Object userId = session.getAttribute("userId");

                Object userRole = session.getAttribute("userRole");

                Object userStatus = session.getAttribute("userStatus");

                if (userId == null ||
                                userRole == null ||
                                userStatus == null) {

                        sendError(
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentication required.");
                        return;
                }

                Map<String, Object> userResponse = new LinkedHashMap<>();

                userResponse.put(
                                "id",
                                userId);

                userResponse.put(
                                "role",
                                userRole);

                userResponse.put(
                                "status",
                                userStatus);

                Map<String, Object> responseBody = new LinkedHashMap<>();

                responseBody.put(
                                "success",
                                true);

                responseBody.put(
                                "message",
                                "Authenticated user.");

                responseBody.put(
                                "user",
                                userResponse);

                response.setStatus(
                                HttpServletResponse.SC_OK);

                objectMapper.writeValue(
                                response.getWriter(),
                                responseBody);
        }
}