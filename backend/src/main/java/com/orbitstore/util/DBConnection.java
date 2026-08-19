package com.orbitstore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/orbit_store"
                    + "?useSSL=false"
                    + "&serverTimezone=Asia/Kolkata"
                    + "&allowPublicKeyRetrieval=true";

    private static final String USER =
            System.getenv("ORBIT_DB_USER");

    private static final String PASSWORD =
            System.getenv("ORBIT_DB_PASSWORD");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "MySQL JDBC Driver could not be loaded."
            );
        }
    }

    private DBConnection() {
        // Prevent instantiation
    }

    public static Connection getConnection() throws SQLException {

        if (USER == null || USER.isBlank()) {
            throw new SQLException(
                    "Database username is not configured."
            );
        }

        if (PASSWORD == null) {
            throw new SQLException(
                    "Database password is not configured."
            );
        }

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}