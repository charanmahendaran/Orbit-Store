package com.orbitstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orbitstore.model.Product;
import com.orbitstore.service.ProductService;
import com.orbitstore.service.ProductServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class ProductServlet extends HttpServlet {

    private final ProductService productService = new ProductServiceImpl();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String idParameter = request.getParameter("id");

            String categoryIdParameter = request.getParameter("categoryId");

            String statusParameter = request.getParameter("status");

            String slugParameter = request.getParameter("slug");

            String skuParameter = request.getParameter("sku");

            if (idParameter != null) {

                Long id = parseLongParameter(
                        idParameter,
                        "id");

                Product product = productService.getProductById(id);

                if (product == null) {
                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Product not found.");
                    return;
                }

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        product);
                return;
            }

            if (categoryIdParameter != null) {

                Long categoryId = parseLongParameter(
                        categoryIdParameter,
                        "categoryId");

                List<Product> products = productService.getProductsByCategoryId(
                        categoryId);

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        products);
                return;
            }

            if (statusParameter != null) {

                Product.Status status;

                try {
                    status = Product.Status.valueOf(
                            statusParameter.toUpperCase());
                } catch (IllegalArgumentException e) {

                    sendError(
                            response,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid product status.");
                    return;
                }

                List<Product> products = productService.getProductsByStatus(status);

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        products);
                return;
            }

            if (slugParameter != null) {

                Product product = productService.getProductBySlug(
                        slugParameter);

                if (product == null) {
                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Product not found.");
                    return;
                }

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        product);
                return;
            }

            if (skuParameter != null) {

                Product product = productService.getProductBySku(
                        skuParameter);

                if (product == null) {
                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Product not found.");
                    return;
                }

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        product);
                return;
            }

            List<Product> products = productService.getAllProducts();

            sendJson(
                    response,
                    HttpServletResponse.SC_OK,
                    products);

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid numeric parameter.");

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An internal server error occurred.");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Product product = objectMapper.readValue(
                    request.getReader(),
                    Product.class);

            Product createdProduct = productService.createProduct(product);

            sendJson(
                    response,
                    HttpServletResponse.SC_CREATED,
                    createdProduct);

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An internal server error occurred.");
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Product product = objectMapper.readValue(
                    request.getReader(),
                    Product.class);

            boolean updated = productService.updateProduct(product);

            if (!updated) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Product not found.");
                return;
            }

            Product updatedProduct = productService.getProductById(
                    product.getId());

            sendJson(
                    response,
                    HttpServletResponse.SC_OK,
                    updatedProduct);

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An internal server error occurred.");
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String idParameter = request.getParameter("id");

            if (idParameter == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Product id is required.");
                return;
            }

            Long id = parseLongParameter(
                    idParameter,
                    "id");

            boolean deleted = productService.deleteProduct(id);

            if (!deleted) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Product not found.");
                return;
            }

            sendJson(
                    response,
                    HttpServletResponse.SC_OK,
                    new MessageResponse(
                            "Product deleted successfully."));

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An internal server error occurred.");
        }
    }

    private Long parseLongParameter(
            String value,
            String parameterName) {

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {

            throw new NumberFormatException(
                    "Invalid " + parameterName + " parameter.");
        }
    }

    private void sendJson(
            HttpServletResponse response,
            int status,
            Object data)
            throws IOException {

        response.setStatus(status);

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

        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse(message));
    }

    private static class ErrorResponse {

        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }

    private static class MessageResponse {

        private final String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}