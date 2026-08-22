package com.orbitstore.service;

import com.orbitstore.model.Cart;
import com.orbitstore.model.CartItem;

import java.sql.SQLException;
import java.util.List;

public interface CartService {

    Cart getOrCreateCart(Long userId) throws SQLException;

    List<CartItem> getCartItems(Long userId) throws SQLException;

    CartItem addItem(Long userId, Long productId, Integer quantity) throws SQLException;

    CartItem updateItemQuantity(
            Long userId,
            Long cartItemId,
            Integer quantity) throws SQLException;

    boolean removeItem(Long userId, Long cartItemId) throws SQLException;

    boolean clearCart(Long userId) throws SQLException;
}