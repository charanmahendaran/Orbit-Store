package com.orbitstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orbitstore.model.Wishlist;
import com.orbitstore.model.WishlistItem;
import com.orbitstore.service.WishlistService;
import com.orbitstore.service.WishlistServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WishlistServlet extends HttpServlet {

    private final WishlistService wishlistService = new WishlistServiceImpl();

    /*
     * Orbit Store JSON convention:
     *
     * LocalDateTime values must be serialized as ISO-8601 strings:
     *
     * "createdAt": "2026-08-22T18:08:10"
     *
     * and NOT as timestamp arrays.
     */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(
                    new JavaTimeModule())
            .disable(
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = getAuthenticatedUserId(request);

        if (userId == null) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required.");

            return;
        }

        try {

            Wishlist wishlist = wishlistService
                    .getOrCreateWishlist(userId);

            List<WishlistItem> items = wishlistService
                    .getWishlistItems(userId);

            Map<String, Object> result = new HashMap<>();

            result.put("wishlist", wishlist);
            result.put("items", items);

            sendSuccess(
                    response,
                    result);

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to retrieve wishlist.");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = getAuthenticatedUserId(request);

        if (userId == null) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required.");

            return;
        }

        try {

            WishlistItemRequest itemRequest = objectMapper.readValue(
                    request.getReader(),
                    WishlistItemRequest.class);

            if (itemRequest.productId == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "productId is required.");

                return;
            }

            WishlistItem item = wishlistService.addItem(
                    userId,
                    itemRequest.productId);

            sendSuccess(
                    response,
                    HttpServletResponse.SC_CREATED,
                    item);

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to add item to wishlist.");
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = getAuthenticatedUserId(request);

        if (userId == null) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required.");

            return;
        }

        try {

            String pathInfo = request.getPathInfo();

            /*
             * DELETE /api/wishlist
             *
             * Clears all wishlist items while
             * preserving the wishlist record.
             */
            if (pathInfo == null
                    || pathInfo.equals("/")
                    || pathInfo.isBlank()) {

                wishlistService.clearWishlist(
                        userId);

                Map<String, Object> result = new HashMap<>();

                result.put(
                        "message",
                        "Wishlist cleared successfully.");

                sendSuccess(
                        response,
                        result);

                return;
            }

            /*
             * DELETE /api/wishlist/{wishlistItemId}
             *
             * Removes one wishlist item.
             */
            Long wishlistItemId = extractId(request);

            if (wishlistItemId == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Wishlist item ID is required.");

                return;
            }

            boolean removed = wishlistService.removeItem(
                    userId,
                    wishlistItemId);

            if (!removed) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Wishlist item not found.");

                return;
            }

            Map<String, Object> result = new HashMap<>();

            result.put(
                    "message",
                    "Wishlist item removed successfully.");

            sendSuccess(
                    response,
                    result);

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to modify wishlist.");
        }
    }

    private Long getAuthenticatedUserId(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object userId = session.getAttribute("userId");

        if (userId instanceof Number) {

            return ((Number) userId).longValue();
        }

        if (userId instanceof String) {

            try {

                return Long.parseLong(
                        (String) userId);

            } catch (NumberFormatException ignored) {

                return null;
            }
        }

        return null;
    }

    private Long extractId(
            HttpServletRequest request) {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null
                || pathInfo.equals("/")) {

            return null;
        }

        String value = pathInfo.substring(1);

        if (value.contains("/")) {

            value = value.substring(
                    0,
                    value.indexOf("/"));
        }

        try {

            return Long.parseLong(value);

        } catch (NumberFormatException e) {

            return null;
        }
    }

    private void sendSuccess(
            HttpServletResponse response,
            Object data)
            throws IOException {

        sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                data);
    }

    private void sendSuccess(
            HttpServletResponse response,
            int status,
            Object data)
            throws IOException {

        response.setStatus(status);
        response.setContentType(
                "application/json");
        response.setCharacterEncoding(
                "UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                data);
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType(
                "application/json");
        response.setCharacterEncoding(
                "UTF-8");

        Map<String, Object> error = new HashMap<>();

        error.put(
                "error",
                message);

        objectMapper.writeValue(
                response.getWriter(),
                error);
    }

    public static class WishlistItemRequest {

        public Long productId;

        public WishlistItemRequest() {
        }
    }
}