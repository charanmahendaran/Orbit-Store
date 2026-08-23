package com.orbitstore.model;

import java.time.LocalDateTime;

public class WishlistItem {

    private Long id;
    private Long wishlistId;
    private Long productId;
    private LocalDateTime addedAt;

    public WishlistItem() {
    }

    public WishlistItem(
            Long id,
            Long wishlistId,
            Long productId,
            LocalDateTime addedAt) {

        this.id = id;
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.addedAt = addedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(Long wishlistId) {
        this.wishlistId = wishlistId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}