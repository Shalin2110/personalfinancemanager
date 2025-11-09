package com.example.service;

import com.example.dao.CategoryDao;
import com.example.model.Category;
import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CategoryService {
    private final CategoryDao dao = new CategoryDao();

    // Add new Category
    public void addCategory(Category category) {
        try {
            // Set the current user ID before adding
            category.setUserId(UserService.getCurrentUserId());
            dao.addCategory(category);
            System.out.println("[CategoryService] Category added successfully.");
        } catch (SQLException e) {
            System.err.println("[CategoryService] Error adding category: " + e.getMessage());
        }
    }

    // Update existing Category
    public void updateCategory(Category category) {
        try {
            dao.updateCategory(category);
            System.out.println("[CategoryService] Category updated successfully.");
        } catch (SQLException e) {
            System.err.println("[CategoryService] Error updating category: " + e.getMessage());
        }
    }

    // Delete (soft delete)
    public void deleteCategory(int id) {
        try {
            dao.deleteCategory(id);
            System.out.println("[CategoryService] Category deleted successfully.");
        } catch (SQLException e) {
            System.err.println("[CategoryService] Error deleting category: " + e.getMessage());
        }
    }

    // Get all categories for current user
    public List<Category> getAllCategories() {
        try {
            int userId = UserService.getCurrentUserId();
            if (userId == -1) return List.of(); // No user logged in
            return dao.getCategoriesByUser(userId);
        } catch (SQLException e) {
            System.err.println("[CategoryService] Error fetching categories: " + e.getMessage());
            return List.of();
        }
    }

    // Get categories for dropdown (parent categories)
    public List<Category> getCategoriesForDropdown() {
        try {
            int userId = UserService.getCurrentUserId();
            if (userId == -1) return List.of(); // No user logged in
            return dao.getCategoriesForDropdown(userId);
        } catch (SQLException e) {
            System.err.println("[CategoryService] Error fetching categories for dropdown: " + e.getMessage());
            return List.of();
        }
    }

    // Get category map for dropdowns
    public Map<String, Integer> getCategoryMap() {
        Map<String, Integer> categories = new HashMap<>();
        try {
            List<Category> categoryList = getAllCategories(); // Use getAllCategories which filters by user
            for (Category category : categoryList) {
                categories.put(category.getName(), category.getCategoryId());
            }
        } catch (Exception e) {
            System.err.println("[CategoryService] Error loading categories: " + e.getMessage());
        }
        return categories;
    }

    public int countCategories() {
        return getAllCategories().size();
    }
}