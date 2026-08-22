package com.orbitstore.service;

import com.orbitstore.model.ProductImage;

import java.sql.SQLException;
import java.util.List;

public interface ProductImageService {

    ProductImage create(ProductImage productImage) throws SQLException;

    ProductImage findById(Long id) throws SQLException;

    List<ProductImage> findByProductId(Long productId) throws SQLException;

    boolean update(ProductImage productImage) throws SQLException;

    boolean delete(Long id) throws SQLException;
}