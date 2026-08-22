package com.orbitstore.dao;

import com.orbitstore.model.ProductImage;

import java.util.List;

public interface ProductImageDAO {

    ProductImage create(ProductImage productImage);

    ProductImage findById(Long id);

    List<ProductImage> findByProductId(Long productId);

    ProductImage findPrimaryByProductId(Long productId);

    boolean update(ProductImage productImage);

    boolean delete(Long id);

    boolean clearPrimaryImages(Long productId);
}