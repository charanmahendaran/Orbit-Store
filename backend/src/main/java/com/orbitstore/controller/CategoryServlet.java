package com.orbitstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orbitstore.model.Category;
import com.orbitstore.service.CategoryService;
import com.orbitstore.service.CategoryServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class CategoryServlet extends HttpServlet {

    private final CategoryService categoryService = new CategoryServiceImpl();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String idParameter = request.getParameter("id");
            String parentIdParameter = request.getParameter("parentId");
            String statusParameter = request.getParameter("status");

            if (idParameter != null) {

                Long id = parseLongParameter(
                        idParameter,
                        "id");

                Category category = categoryService.getCategoryById(id);

                if (category == null) {
                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Category not found.");
                    return;
                }

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        category);
                return;
            }

            if (parentIdParameter != null) {

                Long parentId = parseLongParameter(
                        parentIdParameter,
                        "parentId");

                List<Category> categories = categoryService.getCategoriesByParentId(parentId);

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        categories);
                return;
            }

            if (statusParameter != null) {

                Category.Status status;

                try {
                    status = Category.Status.valueOf(
                            statusParameter.toUpperCase());
                } catch (IllegalArgumentException e) {

                    sendError(
                            response,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid category status.");
                    return;
                }

                List<Category> categories = categoryService.getCategoriesByStatus(status);

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        categories);
                return;
            }

            List<Category> categories = categoryService.getAllCategories();

            sendJson(
                    response,
                    HttpServletResponse.SC_OK,
                    categories);

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
            HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Category category = objectMapper.readValue(
                    request.getReader(),
                    Category.class);

            Category createdCategory = categoryService.createCategory(category);

            sendJson(
                    response,
                    HttpServletResponse.SC_CREATED,
                    createdCategory);

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
            HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Category category = objectMapper.readValue(
                    request.getReader(),
                    Category.class);

            boolean updated = categoryService.updateCategory(category);

            if (!updated) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Category not found.");
                return;
            }

            Category updatedCategory = categoryService.getCategoryById(
                    category.getId());

            sendJson(
                    response,
                    HttpServletResponse.SC_OK,
                    updatedCategory);

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
            HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String idParameter = request.getParameter("id");

            if (idParameter == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Category id is required.");
                return;
            }

            Long id = parseLongParameter(
                    idParameter,
                    "id");

            boolean deleted = categoryService.deleteCategory(id);

            if (!deleted) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Category not found.");
                return;
            }

            sendJson(
                    response,
                    HttpServletResponse.SC_OK,
                    new MessageResponse(
                            "Category deleted successfully."));

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
            Object data) throws IOException {

        response.setStatus(status);

        objectMapper.writeValue(
                response.getWriter(),
                data);
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

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