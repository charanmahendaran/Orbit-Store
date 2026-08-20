package com.orbitstore.dao;

import com.orbitstore.model.Product;

import java.sql.SQLException;
import java.util.List;

public interface ProductDAO {

    Product create(Product product) throws SQLException;

    Product findById(Long id) throws SQLException;

    List<Product> findAll() throws SQLException;

    List<Product> findByCategoryId(Long categoryId) throws SQLException;

    List<Product> findByStatus(Product.Status status) throws SQLException;

    Product findBySlug(String slug) throws SQLException;

    Product findBySku(String sku) throws SQLException;

    boolean update(Product product) throws SQLException;

    boolean delete(Long id) throws SQLException;
}