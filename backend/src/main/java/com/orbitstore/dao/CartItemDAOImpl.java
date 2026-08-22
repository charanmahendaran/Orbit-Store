package com.orbitstore.dao;

import com.orbitstore.model.CartItem;
import com.orbitstore.model.Product;
import com.orbitstore.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CartItemDAOImpl implements CartItemDAO {

    private static final String PRODUCT_COLUMNS = """
            p.id AS product_id,
            p.name AS product_name,
            p.slug AS product_slug,
            p.sku AS product_sku,
            p.price AS product_price,
            p.discount_price AS product_discount_price,
            p.stock_quantity AS product_stock_quantity,
            p.status AS product_status
            """;

    @Override
    public List<CartItem> findByCartId(Long cartId) throws SQLException {

        String sql = """
                SELECT
                    ci.id,
                    ci.cart_id,
                    ci.product_id,
                    ci.quantity,
                    ci.added_at AS created_at,
                    ci.updated_at,
                    %s
                FROM cart_items ci
                INNER JOIN products p ON ci.product_id = p.id
                WHERE ci.cart_id = ?
                ORDER BY ci.added_at ASC
                """.formatted(PRODUCT_COLUMNS);

        List<CartItem> items = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapRow(resultSet));
                }
            }
        }

        return items;
    }

    @Override
    public CartItem findByCartIdAndProductId(Long cartId, Long productId)
            throws SQLException {

        String sql = """
                SELECT
                    ci.id,
                    ci.cart_id,
                    ci.product_id,
                    ci.quantity,
                    ci.added_at AS created_at,
                    ci.updated_at,
                    %s
                FROM cart_items ci
                INNER JOIN products p ON ci.product_id = p.id
                WHERE ci.cart_id = ?
                  AND ci.product_id = ?
                """.formatted(PRODUCT_COLUMNS);

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartId);
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
    public CartItem findById(Long cartItemId) throws SQLException {

        String sql = """
                SELECT
                    ci.id,
                    ci.cart_id,
                    ci.product_id,
                    ci.quantity,
                    ci.added_at AS created_at,
                    ci.updated_at,
                    %s
                FROM cart_items ci
                INNER JOIN products p ON ci.product_id = p.id
                WHERE ci.id = ?
                """.formatted(PRODUCT_COLUMNS);

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartItemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public CartItem create(Long cartId, Long productId, Integer quantity)
            throws SQLException {

        String sql = """
                INSERT INTO cart_items (cart_id, product_id, quantity)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, cartId);
            statement.setLong(2, productId);
            statement.setInt(3, quantity);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating cart item failed.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return findById(generatedKeys.getLong(1));
                }
            }
        }

        throw new SQLException("Creating cart item failed: no ID obtained.");
    }

    @Override
    public boolean updateQuantity(Long cartItemId, Integer quantity)
            throws SQLException {

        String sql = """
                UPDATE cart_items
                SET quantity = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setLong(2, cartItemId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Long cartItemId) throws SQLException {

        String sql = """
                DELETE FROM cart_items
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartItemId);

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteByCartId(Long cartId) throws SQLException {

        String sql = """
                DELETE FROM cart_items
                WHERE cart_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartId);

            return statement.executeUpdate() > 0;
        }
    }

    private CartItem mapRow(ResultSet resultSet) throws SQLException {

        CartItem item = new CartItem();

        item.setId(resultSet.getLong("id"));
        item.setCartId(resultSet.getLong("cart_id"));
        item.setProductId(resultSet.getLong("product_id"));
        item.setQuantity(resultSet.getInt("quantity"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            item.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        item.setProductName(resultSet.getString("product_name"));
        item.setProductSlug(resultSet.getString("product_slug"));
        item.setProductSku(resultSet.getString("product_sku"));

        BigDecimal productPrice = resultSet.getBigDecimal("product_price");
        item.setProductPrice(productPrice);

        BigDecimal productDiscountPrice = resultSet.getBigDecimal("product_discount_price");
        item.setProductDiscountPrice(productDiscountPrice);

        item.setProductStockQuantity(
                resultSet.getInt("product_stock_quantity"));

        String status = resultSet.getString("product_status");
        if (status != null) {
            item.setProductStatus(Product.Status.valueOf(status));
        }

        return item;
    }
}