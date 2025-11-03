package com.example.service;

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
}
