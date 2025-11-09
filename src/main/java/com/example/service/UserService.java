package com.example.service;

import com.example.dao.UserDao;
import com.example.model.User;

import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserService {
    private static final UserDao dao = new UserDao(); // Make dao static
    private static User currentUser;

    // Register new user - now static
    public static boolean registerUser(String username, String password, String email) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(hashPassword(password));
            user.setEmail(email);

            boolean success = dao.registerUser(user);
            if (success) {
                System.out.println("[UserService] User registered successfully.");
            }
            return success;
        } catch (SQLException e) {
            System.err.println("[UserService] Error registering user: " + e.getMessage());
            return false;
        }
    }

    // Authenticate user - now static
    public static boolean loginUser(String username, String password) {
        try {
            String passwordHash = hashPassword(password);
            User user = dao.authenticateUser(username, passwordHash);

            if (user != null) {
                currentUser = user;
                System.out.println("[UserService] User logged in successfully: " + username);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[UserService] Error authenticating user: " + e.getMessage());
            return false;
        }
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1; // Return -1 if no user logged in
    }

    // Logout user - already static ✓
    public static void logoutUser() {
        currentUser = null;
    }

    // Check if user is logged in - already static ✓
    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }

    // Password hashing - now static
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}