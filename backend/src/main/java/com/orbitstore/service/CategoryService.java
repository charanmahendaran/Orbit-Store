package com.orbitstore.service;

import com.orbitstore.model.Category;

import java.sql.SQLException;
import java.util.List;

public interface CategoryService {

    Category createCategory(Category category) throws SQLException;

    Category getCategoryById(Long id) throws SQLException;

    List<Category> getAllCategories() throws SQLException;

    List<Category> getCategoriesByParentId(Long parentId) throws SQLException;

    List<Category> getCategoriesByStatus(Category.Status status)
            throws SQLException;

    boolean updateCategory(Category category) throws SQLException;

    boolean deleteCategory(Long id) throws SQLException;
}