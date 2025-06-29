package com.ecommerce;

import spark.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CartService {
    private static class CartItem {
        Product product;
        int quantity;

        CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

    public static List<Map<String, Object>> getCart(Session session) {
        Map<Integer, CartItem> cart = getCartMap(session);
        List<Map<String, Object>> cartItems = new ArrayList<>();
        
        for (CartItem item : cart.values()) {
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("product", item.product);
            cartItem.put("quantity", item.quantity);
            cartItem.put("subtotal", item.product.getPrice() * item.quantity);
            cartItems.add(cartItem);
        }
        
        return cartItems;
    }

    private static Map<Integer, CartItem> getCartMap(Session session) {
        Map<Integer, CartItem> cart = session.attribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.attribute("cart", cart);
        }
        return cart;
    }

    public static void addProduct(Session session, Product product, int quantity) {
        Map<Integer, CartItem> cart = getCartMap(session);
        
        if (cart.containsKey(product.getId())) {
            CartItem item = cart.get(product.getId());
            item.quantity += quantity;
        } else {
            cart.put(product.getId(), new CartItem(product, quantity));
        }
    }

    public static void removeProduct(Session session, int productId) {
        Map<Integer, CartItem> cart = getCartMap(session);
        cart.remove(productId);
    }

    public static void updateQuantity(Session session, int productId, int quantity) {
        Map<Integer, CartItem> cart = getCartMap(session);
        if (cart.containsKey(productId)) {
            if (quantity <= 0) {
                cart.remove(productId);
            } else {
                cart.get(productId).quantity = quantity;
            }
        }
    }

    public static double getTotal(Session session) {
        Map<Integer, CartItem> cart = getCartMap(session);
        return cart.values().stream()
            .mapToDouble(item -> item.product.getPrice() * item.quantity)
            .sum();
    }
}
