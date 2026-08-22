package com.orbitstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orbitstore.model.Cart;
import com.orbitstore.model.CartItem;
import com.orbitstore.service.CartService;
import com.orbitstore.service.CartServiceImpl;

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

public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartServiceImpl();

    /*
     * Orbit Store JSON convention:
     *
     * LocalDateTime values must be serialized as ISO-8601 strings:
     *
     * "createdAt": "2026-08-22T18:08:10"
     *
     * and NOT as timestamp arrays:
     *
     * "createdAt": [2026, 8, 22, 18, 8, 10]
     */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
            Cart cart = cartService.getOrCreateCart(userId);
            List<CartItem> items = cartService.getCartItems(userId);

            Map<String, Object> result = new HashMap<>();

            result.put("cart", cart);
            result.put("items", items);

            sendSuccess(response, result);

        } catch (SQLException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to retrieve cart.");
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
            CartItemRequest itemRequest = objectMapper.readValue(
                    request.getReader(),
                    CartItemRequest.class);

            if (itemRequest.productId == null
                    || itemRequest.quantity == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "productId and quantity are required.");
                return;
            }

            CartItem item = cartService.addItem(
                    userId,
                    itemRequest.productId,
                    itemRequest.quantity);

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
                    "Failed to add item to cart.");
        }
    }

    @Override
    protected void doPut(
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
            Long cartItemId = extractId(request);

            if (cartItemId == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Cart item ID is required.");
                return;
            }

            CartItemRequest itemRequest = objectMapper.readValue(
                    request.getReader(),
                    CartItemRequest.class);

            if (itemRequest.quantity == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "quantity is required.");
                return;
            }

            CartItem item = cartService.updateItemQuantity(
                    userId,
                    cartItemId,
                    itemRequest.quantity);

            sendSuccess(response, item);

        } catch (IllegalArgumentException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (SQLException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to update cart item.");
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
             * DELETE /api/cart
             *
             * Clears the entire cart.
             */
            if (pathInfo == null
                    || pathInfo.equals("/")
                    || pathInfo.isBlank()) {

                cartService.clearCart(userId);

                Map<String, Object> result = new HashMap<>();

                result.put(
                        "message",
                        "Cart cleared successfully.");

                sendSuccess(response, result);
                return;
            }

            /*
             * DELETE /api/cart/{cartItemId}
             *
             * Removes one cart item.
             */
            Long cartItemId = extractId(request);

            if (cartItemId == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Cart item ID is required.");
                return;
            }

            boolean removed = cartService.removeItem(
                    userId,
                    cartItemId);

            if (!removed) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Cart item not found.");
                return;
            }

            Map<String, Object> result = new HashMap<>();

            result.put(
                    "message",
                    "Cart item removed successfully.");

            sendSuccess(response, result);

        } catch (IllegalArgumentException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (SQLException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to modify cart.");
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
                return Long.parseLong((String) userId);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private Long extractId(
            HttpServletRequest request) {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
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
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new HashMap<>();

        error.put("error", message);

        objectMapper.writeValue(
                response.getWriter(),
                error);
    }

    public static class CartItemRequest {

        public Long productId;
        public Integer quantity;

        public CartItemRequest() {
        }
    }
}