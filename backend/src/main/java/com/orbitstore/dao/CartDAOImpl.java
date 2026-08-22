package com.orbitstore.dao;

import com.orbitstore.model.Cart;
import com.orbitstore.util.DBConnection;

import java.sql.*;

public class CartDAOImpl implements CartDAO {

    @Override
    public Cart findByUserId(Long userId) throws SQLException {

        String sql = """
                SELECT id, user_id, created_at, updated_at
                FROM carts
                WHERE user_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
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
    public Cart findById(Long cartId) throws SQLException {

        String sql = """
                SELECT id, user_id, created_at, updated_at
                FROM carts
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public Cart create(Long userId) throws SQLException {

        String sql = """
                INSERT INTO carts (user_id)
                VALUES (?)
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, userId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating cart failed.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long cartId = generatedKeys.getLong(1);
                    return findById(cartId);
                }
            }
        }

        throw new SQLException("Creating cart failed: no ID obtained.");
    }

    private Cart mapRow(ResultSet resultSet) throws SQLException {

        Cart cart = new Cart();

        cart.setId(resultSet.getLong("id"));
        cart.setUserId(resultSet.getLong("user_id"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            cart.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            cart.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return cart;
    }
}