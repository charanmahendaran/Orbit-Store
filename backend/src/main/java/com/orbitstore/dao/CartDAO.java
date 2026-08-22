package com.orbitstore.dao;

import com.orbitstore.model.Cart;

import java.sql.SQLException;

public interface CartDAO {

    Cart findByUserId(Long userId) throws SQLException;

    Cart findById(Long cartId) throws SQLException;

    Cart create(Long userId) throws SQLException;
}