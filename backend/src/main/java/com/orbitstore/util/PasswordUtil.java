package com.orbitstore.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {
        // Prevent instantiation
    }

    public static String hashPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be null or blank.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);

        byte[] hash = deriveKey(
                password.toCharArray(),
                salt,
                ITERATIONS);

        return "PBKDF2$"
                + ITERATIONS
                + "$"
                + Base64.getEncoder().encodeToString(salt)
                + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(
            String password,
            String storedHash) {

        if (password == null || storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split("\\$");

        if (parts.length != 4 || !parts[0].equals("PBKDF2")) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] actualHash = deriveKey(
                    password.toCharArray(),
                    salt,
                    iterations);

            return constantTimeEquals(
                    expectedHash,
                    actualHash);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] deriveKey(
            char[] password,
            byte[] salt,
            int iterations) {

        try {

            PBEKeySpec keySpec = new PBEKeySpec(
                    password,
                    salt,
                    iterations,
                    KEY_LENGTH);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);

            return factory.generateSecret(keySpec).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {

            throw new IllegalStateException(
                    "Password hashing failed.",
                    e);
        }
    }

    private static boolean constantTimeEquals(
            byte[] first,
            byte[] second) {

        if (first == null || second == null) {
            return false;
        }

        if (first.length != second.length) {
            return false;
        }

        int result = 0;

        for (int i = 0; i < first.length; i++) {
            result |= first[i] ^ second[i];
        }

        return result == 0;
    }
}