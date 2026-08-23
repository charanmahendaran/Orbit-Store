package com.orbitstore.dao;

import com.orbitstore.model.Wishlist;

import java.sql.SQLException;

public interface WishlistDAO {

    Wishlist findByUserId(Long userId) throws SQLException;

    Wishlist findById(Long wishlistId) throws SQLException;

    Wishlist create(Long userId) throws SQLException;
}