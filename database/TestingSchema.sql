USE orbit_store;

SELECT
    id,
    first_name,
    last_name,
    email,
    phone,
    role,
    status,
    created_at,
    updated_at
FROM users
ORDER BY id;

SELECT
    id,
    name,
    status
FROM categories
ORDER BY id;

SELECT
    id,
    category_id,
    name,
    slug,
    sku,
    price,
    discount_price,
    stock_quantity,
    status
FROM products
ORDER BY id;

SELECT
    id,
    product_id,
    image_url,
    is_primary,
    display_order
FROM product_images
ORDER BY product_id, display_order, id;

SELECT
    id,
    user_id,
    created_at,
    updated_at
FROM carts
ORDER BY id;

SELECT
    id,
    cart_id,
    product_id,
    quantity,
    added_at,
    updated_at
FROM cart_items
ORDER BY id;

SELECT * FROM cart_items WHERE id = 1;