package com.orbitstore.service;

import com.orbitstore.model.Wishlist;
import com.orbitstore.model.WishlistItem;

import java.sql.SQLException;
import java.util.List;

public interface WishlistService {

    Wishlist getOrCreateWishlist(
            Long userId) throws SQLException;

    List<WishlistItem> getWishlistItems(
            Long userId) throws SQLException;

    WishlistItem addItem(
            Long userId,
            Long productId) throws SQLException;

    boolean removeItem(
            Long userId,
            Long wishlistItemId) throws SQLException;

    boolean clearWishlist(
            Long userId) throws SQLException;
}