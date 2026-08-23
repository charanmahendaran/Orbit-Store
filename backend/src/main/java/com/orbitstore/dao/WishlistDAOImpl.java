package com.orbitstore.dao;

import com.orbitstore.model.Wishlist;
import com.orbitstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class WishlistDAOImpl implements WishlistDAO {

    @Override
    public Wishlist findByUserId(Long userId) throws SQLException {

        String sql = """
                SELECT id, user_id, created_at, updated_at
                FROM wishlists
                WHERE user_id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public Wishlist findById(Long wishlistId) throws SQLException {

        String sql = """
                SELECT id, user_id, created_at, updated_at
                FROM wishlists
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, wishlistId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public Wishlist create(Long userId) throws SQLException {

        String sql = """
                INSERT INTO wishlists (user_id)
                VALUES (?)
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, userId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Creating wishlist failed.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {

                    Long wishlistId = generatedKeys.getLong(1);

                    return findById(wishlistId);
                }
            }
        }

        throw new SQLException(
                "Creating wishlist failed: no ID obtained.");
    }

    private Wishlist mapRow(
            ResultSet resultSet)
            throws SQLException {

        Wishlist wishlist = new Wishlist();

        wishlist.setId(
                resultSet.getLong("id"));

        wishlist.setUserId(
                resultSet.getLong("user_id"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            wishlist.setCreatedAt(
                    createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

        if (updatedAt != null) {
            wishlist.setUpdatedAt(
                    updatedAt.toLocalDateTime());
        }

        return wishlist;
    }
}