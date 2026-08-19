package com.orbitstore.controller;

import com.orbitstore.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setStatus(HttpServletResponse.SC_OK);

        try (Connection connection = DBConnection.getConnection();
             PrintWriter out = response.getWriter()) {

            out.println("""
                    {
                      "status": "UP",
                      "application": "Orbit Store Backend",
                      "database": "CONNECTED"
                    }
                    """);

        } catch (SQLException e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            try (PrintWriter out = response.getWriter()) {

                out.println("""
                        {
                          "status": "DOWN",
                          "application": "Orbit Store Backend",
                          "database": "DISCONNECTED"
                        }
                        """);
            }

            getServletContext().log(
                    "Database health check failed.",
                    e
            );
        }
    }
}