package com.example.dao;

import com.example.model.Category;
import com.example.db.SQLiteConnection;
import com.example.db.OracleConnection;
import com.example.db.SyncManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    // Add new category + auto sync
    public void addCategory(Category category) throws SQLException {
        String txnId = SyncManager.startSync("category", null);
        String sql = "INSERT INTO category (user_id, name, type, parent_category_id, delete_flag) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, category.getUserId());
            ps.setString(2, category.getName());
            ps.setString(3, category.getType());
            if (category.getParentCategoryId() != null)
                ps.setInt(4, category.getParentCategoryId());
            else
                ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int categoryId = rs.getInt(1);
                category.setCategoryId(categoryId);

                // UPDATE: Use the actual category_id as device_txn_id instead of UUID
                String newTxnId = String.valueOf(categoryId);
                SyncManager.updateDeviceTxnId(txnId, newTxnId, "category");
                txnId = newTxnId; // Update local reference
            }

            syncToOracle(category);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Add category failed: " + e.getMessage());
        }
    }

    // Get all categories (for all users - ADMIN only)
    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = """
            SELECT c1.*, c2.name as parent_name 
            FROM category c1 
            LEFT JOIN category c2 ON c1.parent_category_id = c2.category_id 
            WHERE c1.delete_flag = 0 
            ORDER BY c1.category_id DESC
        """;
        try (Connection conn = SQLiteConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setUserId(rs.getInt("user_id"));
                c.setName(rs.getString("name"));
                c.setType(rs.getString("type"));
                c.setParentCategoryId(rs.getInt("parent_category_id"));
                list.add(c);
            }
        }
        return list;
    }

    // Get categories for specific user
    public List<Category> getCategoriesByUser(int userId) throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = """
            SELECT c1.*, c2.name as parent_name 
            FROM category c1 
            LEFT JOIN category c2 ON c1.parent_category_id = c2.category_id 
            WHERE c1.user_id = ? AND c1.delete_flag = 0 
            ORDER BY c1.category_id DESC
        """;
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setUserId(rs.getInt("user_id"));
                c.setName(rs.getString("name"));
                c.setType(rs.getString("type"));
                c.setParentCategoryId(rs.getInt("parent_category_id"));
                list.add(c);
            }
        }
        return list;
    }

    // Update
    public void updateCategory(Category category) throws SQLException {
        // Use the category_id as device_txn_id for updates
        String txnId = String.valueOf(category.getCategoryId());
        SyncManager.startSync("category", txnId);

        String sql = "UPDATE category SET name=?, type=?, parent_category_id=? WHERE category_id=?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getType());
            if (category.getParentCategoryId() != null)
                ps.setInt(3, category.getParentCategoryId());
            else
                ps.setNull(3, Types.INTEGER);
            ps.setInt(4, category.getCategoryId());
            ps.executeUpdate();

            syncToOracle(category);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Update category failed: " + e.getMessage());
        }
    }

    // Soft delete
    public void deleteCategory(int id) throws SQLException {
        // Use the category_id as device_txn_id for deletes
        String txnId = String.valueOf(id);
        SyncManager.startSync("category", txnId);

        String sql = "UPDATE category SET delete_flag=1 WHERE category_id=?";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

            Category c = new Category();
            c.setCategoryId(id);
            c.setDeleteFlag(true);
            syncToOracle(c);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
        }
    }

    // Get categories for dropdown (for parent category selection) - for specific user
    public List<Category> getCategoriesForDropdown() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, name FROM category WHERE delete_flag = 0 ORDER BY name";
        try (Connection conn = SQLiteConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setName(rs.getString("name"));
                list.add(c);
            }
        }
        return list;
    }

    // Get categories for dropdown for specific user
    public List<Category> getCategoriesForDropdown(int userId) throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, name FROM category WHERE user_id = ? AND delete_flag = 0 ORDER BY name";
        try (Connection conn = SQLiteConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setName(rs.getString("name"));
                list.add(c);
            }
        }
        return list;
    }

    // Sync logic for Oracle - Updated to use stored procedure
    public void syncToOracle(Category category) throws SQLException {
        String sql = "{ call system.proc_sync_category(?, ?, ?, ?, ?, ?) }";

        try (Connection conn = OracleConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, category.getCategoryId());
            cs.setInt(2, category.getUserId());
            cs.setString(3, category.getName());
            cs.setString(4, category.getType());
            if (category.getParentCategoryId() != null) {
                cs.setInt(5, category.getParentCategoryId());
            } else {
                cs.setNull(5, Types.INTEGER);
            }
            cs.setInt(6, category.isDeleteFlag() ? 1 : 0);

            cs.execute();
        } catch (SQLException e) {
            System.err.println("[CategoryDao] Oracle sync failed: " + e.getMessage());
            throw e;
        }
    }
}