package com.example.service;

import com.example.dao.CategoryDao;
import com.example.model.Category;
import java.sql.SQLException;
import java.util.List;

import com.example.db.SQLiteConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CategoryService {
    public Map<String, Integer> getCategoryMap() {
        Map<String, Integer> categories = new HashMap<>();
        String sql = "SELECT category_id, name FROM category WHERE delete_flag = 0";
        try (Connection conn = SQLiteConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                categories.put(rs.getString("name"), rs.getInt("category_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
        return categories;
    }
    private final CategoryDao dao = new CategoryDao();

    // Add new Category
    public void addCategory(Category c) {
        try {
            dao.addCategory(c);
            dao.syncToOracle(c);
        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
        }
    }

    // Update existing Category
    public void updateCategory(Category c) {
        try {
            dao.updateCategory(c);
            dao.syncToOracle(c);
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
        }
    }

    // Delete (soft delete)
    public void deleteCategory(int id) {
        try {
            dao.deleteCategory(id);
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
        }
    }

    // Get all categories
    public List<Category> getAllCategories() {
        try {
            return dao.getAllCategories();
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            return List.of();
        }
    }

}
