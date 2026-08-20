package com.orbitstore.service;

import com.orbitstore.dao.CategoryDAO;
import com.orbitstore.dao.CategoryDAOImpl;
import com.orbitstore.model.Category;

import java.sql.SQLException;
import java.util.List;

public class CategoryServiceImpl implements CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryServiceImpl() {
        this.categoryDAO = new CategoryDAOImpl();
    }

    @Override
    public Category createCategory(Category category) throws SQLException {

        validateCategory(category);

        if (category.getStatus() == null) {
            category.setStatus(Category.Status.ACTIVE);
        }

        return categoryDAO.create(category);
    }

    @Override
    public Category getCategoryById(Long id) throws SQLException {

        validateId(id);

        return categoryDAO.findById(id);
    }

    @Override
    public List<Category> getAllCategories() throws SQLException {

        return categoryDAO.findAll();
    }

    @Override
    public List<Category> getCategoriesByParentId(Long parentId)
            throws SQLException {

        validateId(parentId);

        return categoryDAO.findByParentId(parentId);
    }

    @Override
    public List<Category> getCategoriesByStatus(Category.Status status)
            throws SQLException {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Category status cannot be null.");
        }

        return categoryDAO.findByStatus(status);
    }

    @Override
    public boolean updateCategory(Category category) throws SQLException {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category cannot be null.");
        }

        validateId(category.getId());
        validateCategory(category);

        if (category.getStatus() == null) {
            category.setStatus(Category.Status.ACTIVE);
        }

        return categoryDAO.update(category);
    }

    @Override
    public boolean deleteCategory(Long id) throws SQLException {

        validateId(id);

        return categoryDAO.delete(id);
    }

    private void validateCategory(Category category) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category cannot be null.");
        }

        if (category.getName() == null
                || category.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Category name cannot be empty.");
        }

        if (category.getName().length() > 100) {
            throw new IllegalArgumentException(
                    "Category name cannot exceed 100 characters.");
        }

        if (category.getDescription() != null
                && category.getDescription().isBlank()) {

            category.setDescription(null);
        }

        if (category.getImageUrl() != null
                && category.getImageUrl().isBlank()) {

            category.setImageUrl(null);
        }
    }

    private void validateId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Category ID must be a positive value.");
        }
    }
}