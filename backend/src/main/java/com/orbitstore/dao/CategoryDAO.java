package com.orbitstore.dao;

import com.orbitstore.model.Category;

import java.sql.SQLException;
import java.util.List;

public interface CategoryDAO {

    Category create(Category category) throws SQLException;

    Category findById(Long id) throws SQLException;

    List<Category> findAll() throws SQLException;

    List<Category> findByParentId(Long parentId) throws SQLException;

    List<Category> findByStatus(Category.Status status) throws SQLException;

    boolean update(Category category) throws SQLException;

    boolean delete(Long id) throws SQLException;
}