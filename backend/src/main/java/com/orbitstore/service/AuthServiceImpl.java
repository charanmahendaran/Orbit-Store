package com.orbitstore.service;

import com.orbitstore.dao.UserDAO;
import com.orbitstore.model.User;
import com.orbitstore.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;

    public AuthServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User register(
            String firstName,
            String lastName,
            String email,
            String password,
            String phone) throws SQLException {

        firstName = normalizeName(firstName);
        lastName = normalizeName(lastName);
        email = normalizeEmail(email);
        phone = normalizePhone(phone);

        validateRegistrationInput(
                firstName,
                lastName,
                email,
                password);

        if (userDAO.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "An account with this email already exists.");
        }

        String passwordHash = PasswordUtil.hashPassword(password);

        User user = new User();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setPhone(phone);

        // Public registration can only create customers.
        user.setRole(User.Role.CUSTOMER);
        user.setStatus(User.Status.ACTIVE);

        long generatedId = userDAO.create(user);

        user.setId(generatedId);

        return user;
    }

    @Override
    public User login(
            String email,
            String password) throws SQLException {

        email = normalizeEmail(email);

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password is required.");
        }

        Optional<User> optionalUser = userDAO.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid email or password.");
        }

        User user = optionalUser.get();

        if (user.getStatus() != User.Status.ACTIVE) {
            throw new IllegalStateException(
                    "This account is inactive.");
        }

        boolean passwordMatches = PasswordUtil.verifyPassword(
                password,
                user.getPasswordHash());

        if (!passwordMatches) {
            throw new IllegalArgumentException(
                    "Invalid email or password.");
        }

        return user;
    }

    private void validateRegistrationInput(
            String firstName,
            String lastName,
            String email,
            String password) {

        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException(
                    "First name is required.");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException(
                    "Last name is required.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required.");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException(
                    "Invalid email format.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password is required.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }
    }

    private String normalizeName(String value) {

        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private String normalizeEmail(String email) {

        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    private String normalizePhone(String phone) {

        if (phone == null) {
            return null;
        }

        String normalized = phone.trim();

        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isValidEmail(String email) {

        return email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}