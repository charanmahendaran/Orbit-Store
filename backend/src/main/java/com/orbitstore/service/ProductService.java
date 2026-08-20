package com.orbitstore.service;

import com.orbitstore.model.Product;

import java.sql.SQLException;
import java.util.List;

public interface ProductService {

        Product createProduct(Product product) throws SQLException;

        Product getProductById(Long id) throws SQLException;

        List<Product> getAllProducts() throws SQLException;

        List<Product> getProductsByCategoryId(Long categoryId)
                        throws SQLException;

        List<Product> getProductsByStatus(Product.Status status)
                        throws SQLException;

        Product getProductBySlug(String slug) throws SQLException;

        Product getProductBySku(String sku) throws SQLException;

        boolean updateProduct(Product product) throws SQLException;

        boolean deleteProduct(Long id) throws SQLException;
}