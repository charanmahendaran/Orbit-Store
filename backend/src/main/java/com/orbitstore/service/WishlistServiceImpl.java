package com.orbitstore.service;

import com.orbitstore.dao.WishlistDAO;
import com.orbitstore.dao.WishlistDAOImpl;
import com.orbitstore.dao.WishlistItemDAO;
import com.orbitstore.dao.WishlistItemDAOImpl;
import com.orbitstore.model.Wishlist;
import com.orbitstore.model.WishlistItem;

import java.sql.SQLException;
import java.util.List;

public class WishlistServiceImpl
        implements WishlistService {

    private final WishlistDAO wishlistDAO;
    private final WishlistItemDAO wishlistItemDAO;

    public WishlistServiceImpl() {

        this.wishlistDAO = new WishlistDAOImpl();

        this.wishlistItemDAO = new WishlistItemDAOImpl();
    }

    @Override
    public Wishlist getOrCreateWishlist(
            Long userId)
            throws SQLException {

        Wishlist wishlist = wishlistDAO.findByUserId(userId);

        if (wishlist != null) {
            return wishlist;
        }

        return wishlistDAO.create(userId);
    }

    @Override
    public List<WishlistItem> getWishlistItems(
            Long userId)
            throws SQLException {

        Wishlist wishlist = getOrCreateWishlist(userId);

        return wishlistItemDAO.findByWishlistId(
                wishlist.getId());
    }

    @Override
    public WishlistItem addItem(
            Long userId,
            Long productId)
            throws SQLException {

        if (productId == null
                || productId <= 0) {

            throw new IllegalArgumentException(
                    "Product ID must be greater than zero.");
        }

        if (!wishlistItemDAO.productExists(productId)) {

            throw new IllegalArgumentException(
                    "Product not found.");
        }

        Wishlist wishlist = getOrCreateWishlist(userId);

        WishlistItem existingItem = wishlistItemDAO
                .findByWishlistIdAndProductId(
                        wishlist.getId(),
                        productId);

        if (existingItem != null) {

            throw new IllegalArgumentException(
                    "Product is already in the wishlist.");
        }

        return wishlistItemDAO.create(
                wishlist.getId(),
                productId);
    }

    @Override
    public boolean removeItem(
            Long userId,
            Long wishlistItemId)
            throws SQLException {

        if (wishlistItemId == null
                || wishlistItemId <= 0) {

            throw new IllegalArgumentException(
                    "Wishlist item ID must be greater than zero.");
        }

        Wishlist wishlist = getOrCreateWishlist(userId);

        WishlistItem item = wishlistItemDAO.findById(
                wishlistItemId);

        if (item == null) {

            throw new IllegalArgumentException(
                    "Wishlist item not found.");
        }

        if (!wishlist.getId().equals(
                item.getWishlistId())) {

            throw new IllegalArgumentException(
                    "Wishlist item does not belong "
                            + "to the current user.");
        }

        return wishlistItemDAO.delete(
                wishlistItemId);
    }

    @Override
    public boolean clearWishlist(
            Long userId)
            throws SQLException {

        Wishlist wishlist = getOrCreateWishlist(userId);

        wishlistItemDAO.deleteByWishlistId(
                wishlist.getId());

        return true;
    }
}