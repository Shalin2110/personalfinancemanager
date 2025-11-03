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
        String txnId = SyncManager.startSync("category");
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
            if (rs.next()) category.setCategoryId(rs.getInt(1));

            syncToOracle(category);
            SyncManager.markSuccess(txnId);
        } catch (Exception e) {
            SyncManager.markFailure(txnId, e.getMessage());
            throw new SQLException("Add category failed: " + e.getMessage());
        }
    }

    // Get all categories
    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM category WHERE delete_flag = 0 ORDER BY category_id DESC";
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

    // Update
    public void updateCategory(Category category) throws SQLException {
        String txnId = SyncManager.startSync("category");
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
        String txnId = SyncManager.startSync("category");
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

    // Sync logic for Oracle
    private void syncToOracle(Category category) throws SQLException {
        String sql = """
            MERGE INTO category_central t
            USING (SELECT ? AS category_id, ? AS user_id, ? AS name, ? AS type, ? AS parent_category_id, ? AS delete_flag FROM dual) s
            ON (t.category_id = s.category_id)
            WHEN MATCHED THEN
                UPDATE SET t.user_id=s.user_id, t.name=s.name, t.type=s.type, t.parent_category_id=s.parent_category_id, t.delete_flag=s.delete_flag
            WHEN NOT MATCHED THEN
                INSERT (category_id, user_id, name, type, parent_category_id, delete_flag)
                VALUES (s.category_id, s.user_id, s.name, s.type, s.parent_category_id, s.delete_flag)
        """;
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, category.getCategoryId());
            ps.setInt(2, category.getUserId());
            ps.setString(3, category.getName());
            ps.setString(4, category.getType());
            ps.setObject(5, category.getParentCategoryId());
            ps.setInt(6, category.isDeleteFlag() ? 1 : 0);
            ps.executeUpdate();
        }
    }
}
