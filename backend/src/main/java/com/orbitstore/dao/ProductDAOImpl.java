package com.orbitstore.dao;

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

public class ProductDAOImpl implements ProductDAO {

    @Override
    public Product create(Product product) throws SQLException {

        String sql = "INSERT INTO products " +
                "(category_id, name, slug, description, brand, sku, price, " +
                "discount_price, stock_quantity, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, product.getCategoryId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getSlug());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getBrand());
            statement.setString(6, product.getSku());
            statement.setBigDecimal(7, product.getPrice());

            if (product.getDiscountPrice() != null) {
                statement.setBigDecimal(
                        8,
                        product.getDiscountPrice());
            } else {
                statement.setNull(
                        8,
                        java.sql.Types.DECIMAL);
            }

            statement.setInt(
                    9,
                    product.getStockQuantity());

            statement.setString(
                    10,
                    product.getStatus().name());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    product.setId(
                            generatedKeys.getLong(1));
                }
            }
        }

        return findById(product.getId());
    }

    @Override
    public Product findById(Long id) throws SQLException {

        String sql = "SELECT id, category_id, name, slug, description, brand, " +
                "sku, price, discount_price, stock_quantity, status, " +
                "created_at, updated_at " +
                "FROM products " +
                "WHERE id = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public List<Product> findAll() throws SQLException {

        String sql = "SELECT id, category_id, name, slug, description, brand, " +
                "sku, price, discount_price, stock_quantity, status, " +
                "created_at, updated_at " +
                "FROM products " +
                "ORDER BY name ASC";

        List<Product> products = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapRow(resultSet));
            }
        }

        return products;
    }

    @Override
    public List<Product> findByCategoryId(
            Long categoryId) throws SQLException {

        String sql = "SELECT id, category_id, name, slug, description, brand, " +
                "sku, price, discount_price, stock_quantity, status, " +
                "created_at, updated_at " +
                "FROM products " +
                "WHERE category_id = ? " +
                "ORDER BY name ASC";

        List<Product> products = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    products.add(mapRow(resultSet));
                }
            }
        }

        return products;
    }

    @Override
    public List<Product> findByStatus(
            Product.Status status) throws SQLException {

        String sql = "SELECT id, category_id, name, slug, description, brand, " +
                "sku, price, discount_price, stock_quantity, status, " +
                "created_at, updated_at " +
                "FROM products " +
                "WHERE status = ? " +
                "ORDER BY name ASC";

        List<Product> products = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    status.name());

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    products.add(mapRow(resultSet));
                }
            }
        }

        return products;
    }

    @Override
    public Product findBySlug(String slug) throws SQLException {

        String sql = "SELECT id, category_id, name, slug, description, brand, " +
                "sku, price, discount_price, stock_quantity, status, " +
                "created_at, updated_at " +
                "FROM products " +
                "WHERE slug = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, slug);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public Product findBySku(String sku) throws SQLException {

        String sql = "SELECT id, category_id, name, slug, description, brand, " +
                "sku, price, discount_price, stock_quantity, status, " +
                "created_at, updated_at " +
                "FROM products " +
                "WHERE sku = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, sku);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public boolean update(Product product) throws SQLException {

        String sql = "UPDATE products SET " +
                "category_id = ?, " +
                "name = ?, " +
                "slug = ?, " +
                "description = ?, " +
                "brand = ?, " +
                "sku = ?, " +
                "price = ?, " +
                "discount_price = ?, " +
                "stock_quantity = ?, " +
                "status = ? " +
                "WHERE id = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, product.getCategoryId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getSlug());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getBrand());
            statement.setString(6, product.getSku());
            statement.setBigDecimal(7, product.getPrice());

            if (product.getDiscountPrice() != null) {
                statement.setBigDecimal(
                        8,
                        product.getDiscountPrice());
            } else {
                statement.setNull(
                        8,
                        java.sql.Types.DECIMAL);
            }

            statement.setInt(
                    9,
                    product.getStockQuantity());

            statement.setString(
                    10,
                    product.getStatus().name());

            statement.setLong(
                    11,
                    product.getId());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Long id) throws SQLException {

        String sql = "DELETE FROM products WHERE id = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    private Product mapRow(
            ResultSet resultSet) throws SQLException {

        Product product = new Product();

        product.setId(
                resultSet.getLong("id"));

        product.setCategoryId(
                resultSet.getLong("category_id"));

        product.setName(
                resultSet.getString("name"));

        product.setSlug(
                resultSet.getString("slug"));

        product.setDescription(
                resultSet.getString("description"));

        product.setBrand(
                resultSet.getString("brand"));

        product.setSku(
                resultSet.getString("sku"));

        product.setPrice(
                resultSet.getBigDecimal("price"));

        BigDecimal discountPrice = resultSet.getBigDecimal("discount_price");

        if (resultSet.wasNull()) {
            discountPrice = null;
        }

        product.setDiscountPrice(
                discountPrice);

        product.setStockQuantity(
                resultSet.getInt("stock_quantity"));

        product.setStatus(
                Product.Status.valueOf(
                        resultSet.getString("status")));

        Timestamp createdAt = resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            product.setCreatedAt(
                    createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

        if (updatedAt != null) {
            product.setUpdatedAt(
                    updatedAt.toLocalDateTime());
        }

        return product;
    }
}