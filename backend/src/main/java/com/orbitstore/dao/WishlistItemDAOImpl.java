package com.orbitstore.dao;

import com.orbitstore.model.WishlistItem;
import com.orbitstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WishlistItemDAOImpl
        implements WishlistItemDAO {

    @Override
    public List<WishlistItem> findByWishlistId(
            Long wishlistId)
            throws SQLException {

        String sql = """
                SELECT id,
                       wishlist_id,
                       product_id,
                       added_at
                FROM wishlist_items
                WHERE wishlist_id = ?
                ORDER BY added_at ASC, id ASC
                """;

        List<WishlistItem> items = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, wishlistId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    items.add(mapRow(resultSet));
                }
            }
        }

        return items;
    }

    @Override
    public WishlistItem findById(
            Long wishlistItemId)
            throws SQLException {

        String sql = """
                SELECT id,
                       wishlist_id,
                       product_id,
                       added_at
                FROM wishlist_items
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, wishlistItemId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public WishlistItem findByWishlistIdAndProductId(
            Long wishlistId,
            Long productId)
            throws SQLException {

        String sql = """
                SELECT id,
                       wishlist_id,
                       product_id,
                       added_at
                FROM wishlist_items
                WHERE wishlist_id = ?
                  AND product_id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, wishlistId);
            statement.setLong(2, productId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public WishlistItem create(
            Long wishlistId,
            Long productId)
            throws SQLException {

        String sql = """
                INSERT INTO wishlist_items (
                    wishlist_id,
                    product_id
                )
                VALUES (?, ?)
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, wishlistId);
            statement.setLong(2, productId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Creating wishlist item failed.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {

                    Long wishlistItemId = generatedKeys.getLong(1);

                    return findById(wishlistItemId);
                }
            }
        }

        throw new SQLException(
                "Creating wishlist item failed: "
                        + "no ID obtained.");
    }

    @Override
    public boolean delete(
            Long wishlistItemId)
            throws SQLException {

        String sql = """
                DELETE FROM wishlist_items
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, wishlistItemId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteByWishlistId(
            Long wishlistId)
            throws SQLException {

        String sql = """
                DELETE FROM wishlist_items
                WHERE wishlist_id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, wishlistId);

            statement.executeUpdate();

            return true;
        }
    }

    @Override
    public boolean productExists(
            Long productId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM products
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }

    private WishlistItem mapRow(
            ResultSet resultSet)
            throws SQLException {

        WishlistItem item = new WishlistItem();

        item.setId(
                resultSet.getLong("id"));

        item.setWishlistId(
                resultSet.getLong("wishlist_id"));

        item.setProductId(
                resultSet.getLong("product_id"));

        Timestamp addedAt = resultSet.getTimestamp("added_at");

        if (addedAt != null) {
            item.setAddedAt(
                    addedAt.toLocalDateTime());
        }

        return item;
    }
}