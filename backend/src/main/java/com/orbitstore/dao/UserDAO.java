package com.orbitstore.dao;

import com.orbitstore.model.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO {

    long create(User user) throws SQLException;

    Optional<User> findById(long id) throws SQLException;

    Optional<User> findByEmail(String email) throws SQLException;

    void update(User user) throws SQLException;

    void updatePassword(long id, String passwordHash) throws SQLException;

    void updateStatus(long id, User.Status status) throws SQLException;
}