package com.orbitstore.dao;

import com.orbitstore.model.User;
import com.orbitstore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private static final String INSERT_USER = "INSERT INTO users " +
            "(first_name, last_name, email, password_hash, phone, role, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID = "SELECT id, first_name, last_name, email, password_hash, " +
            "phone, role, status, created_at, updated_at " +
            "FROM users WHERE id = ?";

    private static final String FIND_BY_EMAIL = "SELECT id, first_name, last_name, email, password_hash, " +
            "phone, role, status, created_at, updated_at " +
            "FROM users WHERE email = ?";

    private static final String UPDATE_USER = "UPDATE users SET " +
            "first_name = ?, " +
            "last_name = ?, " +
            "email = ?, " +
            "phone = ? " +
            "WHERE id = ?";

    private static final String UPDATE_PASSWORD = "UPDATE users SET password_hash = ? WHERE id = ?";

    private static final String UPDATE_STATUS = "UPDATE users SET status = ? WHERE id = ?";

    @Override
    public long create(User user) throws SQLException {

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        INSERT_USER,
                        Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPasswordHash());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getRole().name());
            statement.setString(7, user.getStatus().name());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed. No rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }

                throw new SQLException(
                        "Creating user failed. No generated ID returned.");
            }
        }
    }

    @Override
    public Optional<User> findById(long id) throws SQLException {

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws SQLException {

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public void update(User user) throws SQLException {

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_USER)) {

            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setLong(5, user.getId());

            statement.executeUpdate();
        }
    }

    @Override
    public void updatePassword(long id, String passwordHash)
            throws SQLException {

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD)) {

            statement.setString(1, passwordHash);
            statement.setLong(2, id);

            statement.executeUpdate();
        }
    }

    @Override
    public void updateStatus(long id, User.Status status)
            throws SQLException {

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS)) {

            statement.setString(1, status.name());
            statement.setLong(2, id);

            statement.executeUpdate();
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {

        User user = new User();

        user.setId(resultSet.getLong("id"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setPhone(resultSet.getString("phone"));

        user.setRole(
                User.Role.valueOf(
                        resultSet.getString("role")));

        user.setStatus(
                User.Status.valueOf(
                        resultSet.getString("status")));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return user;
    }
}