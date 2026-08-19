package com.orbitstore.service;

import com.orbitstore.model.User;

import java.sql.SQLException;

public interface AuthService {

    User register(
            String firstName,
            String lastName,
            String email,
            String password,
            String phone) throws SQLException;

    User login(
            String email,
            String password) throws SQLException;
}