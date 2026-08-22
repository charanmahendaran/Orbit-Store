package com.orbitstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orbitstore.model.ProductImage;
import com.orbitstore.service.ProductImageService;
import com.orbitstore.service.ProductImageServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class ProductImageServlet extends HttpServlet {

    private final ProductImageService productImageService = new ProductImageServiceImpl();

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

            String productIdParameter = request.getParameter("productId");

            /*
             * GET /api/product-images?id=...
             */
            if (idParameter != null) {

                Long id = parseLongParameter(
                        idParameter,
                        "id");

                ProductImage productImage = productImageService.findById(id);

                if (productImage == null) {

                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Product image not found.");

                    return;
                }

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        productImage);

                return;
            }

            /*
             * GET /api/product-images?productId=...
             */
            if (productIdParameter != null) {

                Long productId = parseLongParameter(
                        productIdParameter,
                        "productId");

                List<ProductImage> productImages = productImageService.findByProductId(productId);

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        productImages);

                return;
            }

            /*
             * GET /api/product-images
             *
             * V04 intentionally does not expose a global
             * image listing endpoint because productId is
             * the primary lookup relationship.
             */
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Product id is required.");

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

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

            ProductImage productImage = objectMapper.readValue(
                    request.getReader(),
                    ProductImage.class);

            ProductImage createdProductImage = productImageService.create(productImage);

            sendJson(
                    response,
                    HttpServletResponse.SC_CREATED,
                    createdProductImage);

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

            ProductImage productImage = objectMapper.readValue(
                    request.getReader(),
                    ProductImage.class);

            boolean updated = productImageService.update(productImage);

            if (!updated) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Product image not found.");

                return;
            }

            ProductImage updatedProductImage = productImageService.findById(
                    productImage.getId());

            sendJson(
                    response,
                    HttpServletResponse.SC_OK,
                    updatedProductImage);

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
                        "Product image id is required.");

                return;
            }

            Long id = parseLongParameter(
                    idParameter,
                    "id");

            boolean deleted = productImageService.delete(id);

            if (!deleted) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Product image not found.");

                return;
            }

            sendJson(
                    response,
                    HttpServletResponse.SC_OK,
                    new MessageResponse(
                            "Product image deleted successfully."));

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

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
                    "Invalid "
                            + parameterName
                            + " parameter.");
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