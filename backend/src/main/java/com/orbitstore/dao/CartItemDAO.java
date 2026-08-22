package com.orbitstore.dao;

import com.orbitstore.model.CartItem;

import java.sql.SQLException;
import java.util.List;

public interface CartItemDAO {

    List<CartItem> findByCartId(Long cartId) throws SQLException;

    CartItem findByCartIdAndProductId(Long cartId, Long productId) throws SQLException;

    CartItem findById(Long cartItemId) throws SQLException;

    CartItem create(Long cartId, Long productId, Integer quantity) throws SQLException;

    boolean updateQuantity(Long cartItemId, Integer quantity) throws SQLException;

    boolean delete(Long cartItemId) throws SQLException;

    boolean deleteByCartId(Long cartId) throws SQLException;
}