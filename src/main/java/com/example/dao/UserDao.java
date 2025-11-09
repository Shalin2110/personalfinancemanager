package com.example.dao;

import com.example.model.User;
import com.example.db.SQLiteConnection;
import com.example.db.OracleConnection;
import com.example.db.SyncManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // Register new user (SQLite only for now, will sync to Oracle)
    public boolean registerUser(User user) throws SQLException {
        // Check if username already exists
        if (usernameExists(user.getUsername())) {
            throw new SQLException("Username already exists");
        }

        String sql = "INSERT INTO user (username, password_hash, email, delete_flag) VALUES (?, ?, ?, 0)";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.executeUpdate();

            // Retrieve new ID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
            }

            // Sync to Oracle
            syncToOracle(user);
            return true;
        }
    }

    // Authenticate user (try SQLite first, then Oracle)
    public User authenticateUser(String username, String passwordHash) throws SQLException {
        // Try SQLite first
        User user = authenticateInSQLite(username, passwordHash);
        if (user != null) {
            return user;
        }

        // If not found in SQLite, try Oracle
        return authenticateInOracle(username, passwordHash);
    }

    // SQLite authentication
    private User authenticateInSQLite(String username, String passwordHash) throws SQLException {
        String sql = "SELECT * FROM user WHERE username = ? AND password_hash = ? AND delete_flag = 0";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        }
        return null;
    }

    // Oracle authentication
    private User authenticateInOracle(String username, String passwordHash) throws SQLException {
        String sql = "SELECT * FROM user_central WHERE username = ? AND password_hash = ? AND delete_flag = 0";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                // If user exists in Oracle but not in SQLite, sync them down
                if (!usernameExistsInSQLite(username)) {
                    syncFromOracle(user);
                }
                return user;
            }
        }
        return null;
    }

    // Check if username exists in SQLite
    private boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ? AND delete_flag = 0";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Check if username exists in SQLite
    private boolean usernameExistsInSQLite(String username) throws SQLException {
        return usernameExists(username);
    }

    // Sync user from Oracle to SQLite
    private void syncFromOracle(User user) throws SQLException {
        String sql = "INSERT INTO user (user_id, username, password_hash, email, created_at, delete_flag) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getEmail());
            ps.setTimestamp(5, user.getCreatedAt());
            ps.setInt(6, user.isDeleteFlag() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    // Oracle Sync Logic
    private void syncToOracle(User user) throws SQLException {
        String sql = "{ call proc_sync_user(?, ?, ?, ?, ?, ?) }";

        try (Connection conn = OracleConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, user.getUserId());
            cs.setString(2, user.getUsername());
            cs.setString(3, user.getPasswordHash());
            cs.setString(4, user.getEmail());
            cs.setTimestamp(5, user.getCreatedAt());
            cs.setInt(6, user.isDeleteFlag() ? 1 : 0);

            cs.execute();
        } catch (SQLException e) {
            System.err.println("[UserDao] Oracle sync failed: " + e.getMessage());
            // Don't throw - allow offline registration
        }
    }

    // Helper method to map ResultSet to User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setDeleteFlag(rs.getBoolean("delete_flag"));
        return user;
    }
}