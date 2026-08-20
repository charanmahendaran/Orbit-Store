package com.orbitstore.dao;

import com.orbitstore.model.Category;
import com.orbitstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl implements CategoryDAO {

    private static final String INSERT_CATEGORY_SQL = "INSERT INTO categories " +
            "(name, description, parent_id, image_url, status) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID_SQL = "SELECT id, name, description, parent_id, image_url, status, " +
            "created_at, updated_at " +
            "FROM categories " +
            "WHERE id = ?";

    private static final String FIND_ALL_SQL = "SELECT id, name, description, parent_id, image_url, status, " +
            "created_at, updated_at " +
            "FROM categories " +
            "ORDER BY name ASC";

    private static final String FIND_BY_PARENT_ID_SQL = "SELECT id, name, description, parent_id, image_url, status, " +
            "created_at, updated_at " +
            "FROM categories " +
            "WHERE parent_id = ? " +
            "ORDER BY name ASC";

    private static final String FIND_BY_STATUS_SQL = "SELECT id, name, description, parent_id, image_url, status, " +
            "created_at, updated_at " +
            "FROM categories " +
            "WHERE status = ? " +
            "ORDER BY name ASC";

    private static final String UPDATE_CATEGORY_SQL = "UPDATE categories " +
            "SET name = ?, description = ?, parent_id = ?, image_url = ?, status = ? " +
            "WHERE id = ?";

    private static final String DELETE_CATEGORY_SQL = "DELETE FROM categories WHERE id = ?";

    @Override
    public Category create(Category category) throws SQLException {

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        INSERT_CATEGORY_SQL,
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            if (category.getParentId() != null) {
                statement.setLong(3, category.getParentId());
            } else {
                statement.setNull(3, java.sql.Types.BIGINT);
            }

            statement.setString(4, category.getImageUrl());
            statement.setString(5, category.getStatus().name());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating category failed.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "Creating category failed: no generated ID returned.");
                }

                category.setId(generatedKeys.getLong(1));
            }

            return findById(category.getId());
        }
    }

    @Override
    public Category findById(Long id) throws SQLException {

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_BY_ID_SQL)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }
        }
    }

    @Override
    public List<Category> findAll() throws SQLException {

        List<Category> categories = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_ALL_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(mapRow(resultSet));
            }
        }

        return categories;
    }

    @Override
    public List<Category> findByParentId(Long parentId) throws SQLException {

        List<Category> categories = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_BY_PARENT_ID_SQL)) {
            statement.setLong(1, parentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    categories.add(mapRow(resultSet));
                }
            }
        }

        return categories;
    }

    @Override
    public List<Category> findByStatus(Category.Status status)
            throws SQLException {

        List<Category> categories = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_BY_STATUS_SQL)) {
            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    categories.add(mapRow(resultSet));
                }
            }
        }

        return categories;
    }

    @Override
    public boolean update(Category category) throws SQLException {

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        UPDATE_CATEGORY_SQL)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            if (category.getParentId() != null) {
                statement.setLong(3, category.getParentId());
            } else {
                statement.setNull(3, java.sql.Types.BIGINT);
            }

            statement.setString(4, category.getImageUrl());
            statement.setString(5, category.getStatus().name());
            statement.setLong(6, category.getId());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Long id) throws SQLException {

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        DELETE_CATEGORY_SQL)) {
            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    private Category mapRow(ResultSet resultSet) throws SQLException {

        Category category = new Category();

        category.setId(resultSet.getLong("id"));
        category.setName(resultSet.getString("name"));
        category.setDescription(resultSet.getString("description"));

        long parentId = resultSet.getLong("parent_id");

        if (resultSet.wasNull()) {
            category.setParentId(null);
        } else {
            category.setParentId(parentId);
        }

        category.setImageUrl(resultSet.getString("image_url"));

        String status = resultSet.getString("status");

        if (status != null) {
            category.setStatus(
                    Category.Status.valueOf(status));
        }

        Timestamp createdAt = resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            category.setCreatedAt(
                    createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

        if (updatedAt != null) {
            category.setUpdatedAt(
                    updatedAt.toLocalDateTime());
        }

        return category;
    }
}