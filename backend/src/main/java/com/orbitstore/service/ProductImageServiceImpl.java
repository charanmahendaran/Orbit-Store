package com.orbitstore.service;

import com.orbitstore.dao.ProductDAO;
import com.orbitstore.dao.ProductDAOImpl;
import com.orbitstore.dao.ProductImageDAO;
import com.orbitstore.dao.ProductImageDAOImpl;
import com.orbitstore.model.ProductImage;

import java.sql.SQLException;
import java.util.List;

public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageDAO productImageDAO;
    private final ProductDAO productDAO;

    public ProductImageServiceImpl() {
        this.productImageDAO = new ProductImageDAOImpl();
        this.productDAO = new ProductDAOImpl();
    }

    @Override
    public ProductImage create(ProductImage productImage)
            throws SQLException {

        validateProductImage(productImage);

        if (productDAO.findById(productImage.getProductId()) == null) {
            throw new IllegalArgumentException(
                    "Product does not exist.");
        }

        if (productImage.getIsPrimary() == null) {
            productImage.setIsPrimary(false);
        }

        if (productImage.getDisplayOrder() == null) {
            productImage.setDisplayOrder(0);
        }

        if (productImage.getDisplayOrder() < 0) {
            throw new IllegalArgumentException(
                    "Display order cannot be negative.");
        }

        if (Boolean.TRUE.equals(productImage.getIsPrimary())) {
            productImageDAO.clearPrimaryImages(
                    productImage.getProductId());
        }

        return productImageDAO.create(productImage);
    }

    @Override
    public ProductImage findById(Long id)
            throws SQLException {

        validateId(id, "Image ID");

        return productImageDAO.findById(id);
    }

    @Override
    public List<ProductImage> findByProductId(Long productId)
            throws SQLException {

        validateId(productId, "Product ID");

        if (productDAO.findById(productId) == null) {
            throw new IllegalArgumentException(
                    "Product does not exist.");
        }

        return productImageDAO.findByProductId(productId);
    }

    @Override
    public boolean update(ProductImage productImage)
            throws SQLException {

        if (productImage == null) {
            throw new IllegalArgumentException(
                    "Product image cannot be null.");
        }

        validateId(productImage.getId(), "Image ID");
        validateProductImage(productImage);

        ProductImage existingImage = productImageDAO.findById(productImage.getId());

        if (existingImage == null) {
            return false;
        }

        if (productDAO.findById(productImage.getProductId()) == null) {
            throw new IllegalArgumentException(
                    "Product does not exist.");
        }

        if (productImage.getIsPrimary() == null) {
            productImage.setIsPrimary(false);
        }

        if (productImage.getDisplayOrder() == null) {
            productImage.setDisplayOrder(0);
        }

        if (productImage.getDisplayOrder() < 0) {
            throw new IllegalArgumentException(
                    "Display order cannot be negative.");
        }

        if (Boolean.TRUE.equals(productImage.getIsPrimary())) {
            productImageDAO.clearPrimaryImages(
                    productImage.getProductId());
        }

        return productImageDAO.update(productImage);
    }

    @Override
    public boolean delete(Long id)
            throws SQLException {

        validateId(id, "Image ID");

        ProductImage existingImage = productImageDAO.findById(id);

        if (existingImage == null) {
            return false;
        }

        return productImageDAO.delete(id);
    }

    private void validateProductImage(ProductImage productImage) {

        if (productImage == null) {
            throw new IllegalArgumentException(
                    "Product image cannot be null.");
        }

        if (productImage.getProductId() == null
                || productImage.getProductId() <= 0) {

            throw new IllegalArgumentException(
                    "Product ID must be a positive value.");
        }

        if (productImage.getImageUrl() == null
                || productImage.getImageUrl().isBlank()) {

            throw new IllegalArgumentException(
                    "Image URL is required.");
        }

        if (productImage.getImageUrl().length() > 1000) {
            throw new IllegalArgumentException(
                    "Image URL cannot exceed 1000 characters.");
        }
    }

    private void validateId(Long id, String fieldName) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be a positive value.");
        }
    }
}