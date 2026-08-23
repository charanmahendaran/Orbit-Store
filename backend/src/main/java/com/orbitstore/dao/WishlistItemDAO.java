package com.orbitstore.dao;

import com.orbitstore.model.WishlistItem;

import java.sql.SQLException;
import java.util.List;

public interface WishlistItemDAO {

    List<WishlistItem> findByWishlistId(
            Long wishlistId) throws SQLException;

    WishlistItem findById(
            Long wishlistItemId) throws SQLException;

    WishlistItem findByWishlistIdAndProductId(
            Long wishlistId,
            Long productId) throws SQLException;

    WishlistItem create(
            Long wishlistId,
            Long productId) throws SQLException;

    boolean delete(
            Long wishlistItemId) throws SQLException;

    boolean deleteByWishlistId(
            Long wishlistId) throws SQLException;

    boolean productExists(
            Long productId) throws SQLException;
}