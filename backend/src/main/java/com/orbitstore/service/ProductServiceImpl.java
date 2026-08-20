package com.orbitstore.service;

import com.orbitstore.dao.ProductDAO;
import com.orbitstore.dao.ProductDAOImpl;
import com.orbitstore.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO = new ProductDAOImpl();

    @Override
    public Product createProduct(Product product) throws SQLException {

        validateProduct(product, false);

        if (productDAO.findBySlug(product.getSlug()) != null) {
            throw new IllegalArgumentException(
                    "Product slug already exists.");
        }

        if (productDAO.findBySku(product.getSku()) != null) {
            throw new IllegalArgumentException(
                    "Product SKU already exists.");
        }

        if (product.getStatus() == null) {
            product.setStatus(Product.Status.ACTIVE);
        }

        return productDAO.create(product);
    }

    @Override
    public Product getProductById(Long id) throws SQLException {

        validateId(id, "Product ID");

        return productDAO.findById(id);
    }

    @Override
    public List<Product> getAllProducts() throws SQLException {
        return productDAO.findAll();
    }

    @Override
    public List<Product> getProductsByCategoryId(
            Long categoryId) throws SQLException {

        validateId(categoryId, "Category ID");

        return productDAO.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> getProductsByStatus(
            Product.Status status) throws SQLException {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Product status is required.");
        }

        return productDAO.findByStatus(status);
    }

    @Override
    public Product getProductBySlug(
            String slug) throws SQLException {

        validateSlug(slug);

        return productDAO.findBySlug(slug);
    }

    @Override
    public Product getProductBySku(
            String sku) throws SQLException {

        validateSku(sku);

        return productDAO.findBySku(sku);
    }

    @Override
    public boolean updateProduct(
            Product product) throws SQLException {

        validateProduct(product, true);

        Product existingProduct = productDAO.findById(product.getId());

        if (existingProduct == null) {
            return false;
        }

        Product existingSlug = productDAO.findBySlug(product.getSlug());

        if (existingSlug != null
                && !existingSlug.getId().equals(product.getId())) {

            throw new IllegalArgumentException(
                    "Product slug already exists.");
        }

        Product existingSku = productDAO.findBySku(product.getSku());

        if (existingSku != null
                && !existingSku.getId().equals(product.getId())) {

            throw new IllegalArgumentException(
                    "Product SKU already exists.");
        }

        return productDAO.update(product);
    }

    @Override
    public boolean deleteProduct(Long id) throws SQLException {

        validateId(id, "Product ID");

        return productDAO.delete(id);
    }

    private void validateProduct(
            Product product,
            boolean update) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product data is required.");
        }

        if (update) {
            validateId(
                    product.getId(),
                    "Product ID");
        }

        validateId(
                product.getCategoryId(),
                "Category ID");

        if (product.getName() == null
                || product.getName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Product name cannot be empty.");
        }

        if (product.getName().length() > 255) {
            throw new IllegalArgumentException(
                    "Product name cannot exceed 255 characters.");
        }

        validateSlug(product.getSlug());

        if (product.getSlug().length() > 255) {
            throw new IllegalArgumentException(
                    "Product slug cannot exceed 255 characters.");
        }

        if (product.getBrand() != null
                && product.getBrand().length() > 100) {

            throw new IllegalArgumentException(
                    "Product brand cannot exceed 100 characters.");
        }

        validateSku(product.getSku());

        if (product.getSku().length() > 100) {
            throw new IllegalArgumentException(
                    "Product SKU cannot exceed 100 characters.");
        }

        if (product.getPrice() == null) {
            throw new IllegalArgumentException(
                    "Product price is required.");
        }

        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Product price cannot be negative.");
        }

        if (product.getDiscountPrice() != null) {

            if (product.getDiscountPrice()
                    .compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "Product discount price cannot be negative.");
            }

            if (product.getDiscountPrice()
                    .compareTo(product.getPrice()) > 0) {

                throw new IllegalArgumentException(
                        "Product discount price cannot exceed product price.");
            }
        }

        if (product.getStockQuantity() == null) {
            throw new IllegalArgumentException(
                    "Product stock quantity is required.");
        }

        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Product stock quantity cannot be negative.");
        }

        if (product.getStatus() == null) {
            product.setStatus(Product.Status.ACTIVE);
        }
    }

    private void validateId(
            Long id,
            String fieldName) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be a positive value.");
        }
    }

    private void validateSlug(String slug) {

        if (slug == null
                || slug.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Product slug cannot be empty.");
        }
    }

    private void validateSku(String sku) {

        if (sku == null
                || sku.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Product SKU cannot be empty.");
        }
    }
}