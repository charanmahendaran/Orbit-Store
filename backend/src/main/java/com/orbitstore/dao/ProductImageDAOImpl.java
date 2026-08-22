package com.orbitstore.dao;

import com.orbitstore.model.ProductImage;
import com.orbitstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProductImageDAOImpl implements ProductImageDAO {

    @Override
    public ProductImage create(ProductImage productImage) {

        String sql = """
                INSERT INTO product_images
                (product_id, image_url, is_primary, display_order)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, productImage.getProductId());
            statement.setString(2, productImage.getImageUrl());
            statement.setBoolean(3, Boolean.TRUE.equals(productImage.getIsPrimary()));
            statement.setInt(4, productImage.getDisplayOrder() == null
                    ? 0
                    : productImage.getDisplayOrder());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                return null;
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    productImage.setId(generatedKeys.getLong(1));
                }
            }

            return findById(productImage.getId());

        } catch (Exception e) {
            throw new RuntimeException("Failed to create product image", e);
        }
    }

    @Override
    public ProductImage findById(Long id) {

        String sql = """
                SELECT
                    id,
                    product_id,
                    image_url,
                    is_primary,
                    display_order,
                    created_at
                FROM product_images
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to find product image by ID", e);
        }
    }

    @Override
    public List<ProductImage> findByProductId(Long productId) {

        String sql = """
                SELECT
                    id,
                    product_id,
                    image_url,
                    is_primary,
                    display_order,
                    created_at
                FROM product_images
                WHERE product_id = ?
                ORDER BY display_order ASC, id ASC
                """;

        List<ProductImage> images = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    images.add(mapRow(resultSet));
                }
            }

            return images;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to find product images by product ID",
                    e);
        }
    }

    @Override
    public ProductImage findPrimaryByProductId(Long productId) {

        String sql = """
                SELECT
                    id,
                    product_id,
                    image_url,
                    is_primary,
                    display_order,
                    created_at
                FROM product_images
                WHERE product_id = ?
                  AND is_primary = TRUE
                ORDER BY id ASC
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to find primary product image",
                    e);
        }
    }

    @Override
    public boolean update(ProductImage productImage) {

        String sql = """
                UPDATE product_images
                SET
                    product_id = ?,
                    image_url = ?,
                    is_primary = ?,
                    display_order = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productImage.getProductId());
            statement.setString(2, productImage.getImageUrl());
            statement.setBoolean(
                    3,
                    Boolean.TRUE.equals(productImage.getIsPrimary()));
            statement.setInt(
                    4,
                    productImage.getDisplayOrder() == null
                            ? 0
                            : productImage.getDisplayOrder());
            statement.setLong(5, productImage.getId());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Failed to update product image", e);
        }
    }

    @Override
    public boolean delete(Long id) {

        String sql = """
                DELETE FROM product_images
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product image", e);
        }
    }

    @Override
    public boolean clearPrimaryImages(Long productId) {

        String sql = """
                UPDATE product_images
                SET is_primary = FALSE
                WHERE product_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            statement.executeUpdate();

            return true;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to clear primary product images",
                    e);
        }
    }

    private ProductImage mapRow(ResultSet resultSet) throws Exception {

        ProductImage productImage = new ProductImage();

        productImage.setId(resultSet.getLong("id"));
        productImage.setProductId(resultSet.getLong("product_id"));
        productImage.setImageUrl(resultSet.getString("image_url"));
        productImage.setIsPrimary(resultSet.getBoolean("is_primary"));
        productImage.setDisplayOrder(resultSet.getInt("display_order"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            productImage.setCreatedAt(createdAt.toLocalDateTime());
        }

        return productImage;
    }
}