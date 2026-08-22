package com.orbitstore.service;

import com.orbitstore.dao.CartDAO;
import com.orbitstore.dao.CartDAOImpl;
import com.orbitstore.dao.CartItemDAO;
import com.orbitstore.dao.CartItemDAOImpl;
import com.orbitstore.model.Cart;
import com.orbitstore.model.CartItem;

import java.sql.SQLException;
import java.util.List;

public class CartServiceImpl implements CartService {

    private final CartDAO cartDAO;
    private final CartItemDAO cartItemDAO;

    public CartServiceImpl() {
        this.cartDAO = new CartDAOImpl();
        this.cartItemDAO = new CartItemDAOImpl();
    }

    @Override
    public Cart getOrCreateCart(Long userId) throws SQLException {

        Cart cart = cartDAO.findByUserId(userId);

        if (cart != null) {
            return cart;
        }

        return cartDAO.create(userId);
    }

    @Override
    public List<CartItem> getCartItems(Long userId) throws SQLException {

        Cart cart = getOrCreateCart(userId);

        return cartItemDAO.findByCartId(cart.getId());
    }

    @Override
    public CartItem addItem(
            Long userId,
            Long productId,
            Integer quantity) throws SQLException {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }

        Cart cart = getOrCreateCart(userId);

        CartItem existingItem = cartItemDAO.findByCartIdAndProductId(
                cart.getId(),
                productId);

        if (existingItem != null) {

            int newQuantity = existingItem.getQuantity() + quantity;

            if (existingItem.getProductStockQuantity() != null
                    && newQuantity > existingItem.getProductStockQuantity()) {

                throw new IllegalArgumentException(
                        "Requested quantity exceeds available stock.");
            }

            cartItemDAO.updateQuantity(
                    existingItem.getId(),
                    newQuantity);

            return cartItemDAO.findById(existingItem.getId());
        }

        return cartItemDAO.create(
                cart.getId(),
                productId,
                quantity);
    }

    @Override
    public CartItem updateItemQuantity(
            Long userId,
            Long cartItemId,
            Integer quantity) throws SQLException {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }

        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemDAO.findById(cartItemId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Cart item not found.");
        }

        if (!cart.getId().equals(item.getCartId())) {
            throw new IllegalArgumentException(
                    "Cart item does not belong to the current user.");
        }

        if (item.getProductStockQuantity() != null
                && quantity > item.getProductStockQuantity()) {

            throw new IllegalArgumentException(
                    "Requested quantity exceeds available stock.");
        }

        boolean updated = cartItemDAO.updateQuantity(
                cartItemId,
                quantity);

        if (!updated) {
            throw new SQLException(
                    "Failed to update cart item.");
        }

        return cartItemDAO.findById(cartItemId);
    }

    @Override
    public boolean removeItem(
            Long userId,
            Long cartItemId) throws SQLException {

        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemDAO.findById(cartItemId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Cart item not found.");
        }

        if (!cart.getId().equals(item.getCartId())) {
            throw new IllegalArgumentException(
                    "Cart item does not belong to the current user.");
        }

        return cartItemDAO.delete(cartItemId);
    }

    @Override
    public boolean clearCart(Long userId) throws SQLException {

        Cart cart = getOrCreateCart(userId);

        cartItemDAO.deleteByCartId(cart.getId());

        return true;
    }
}