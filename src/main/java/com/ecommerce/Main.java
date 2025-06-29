package com.ecommerce;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Session;

import java.util.*;
import static spark.Spark.*;

public class Main {
    private static final List<Product> products = new ArrayList<>();
    private static final String CSRF_TOKEN_KEY = "csrf_token";

    public static void main(String[] args) {
        initializeProducts();
        setupRoutes();
    }

    private static void initializeProducts() {
        products.add(new Product(1, "Laptop", 1200.00, "https://ik.imagekit.io/yfork8zzq/Copilot_20250628_112820.png?updatedAt=1751091025801", 
            "High-performance laptop with 16GB RAM and 512GB SSD", 10, "Electronics"));
        products.add(new Product(2, "Smartphone", 800.00, "https://ik.imagekit.io/yfork8zzq/Copilot_20250628_113002.png?updatedAt=1751091025814",
            "Latest smartphone with 5G capability", 15, "Electronics"));
        products.add(new Product(3, "Headphones", 150.00, "https://ik.imagekit.io/yfork8zzq/Copilot_20250628_113251.png?updatedAt=1751091027735",
            "Noise-cancelling wireless headphones", 20, "Audio"));
        products.add(new Product(4, "Keyboard", 75.00, "https://ik.imagekit.io/yfork8zzq/Copilot_20250628_113437.png?updatedAt=1751091027362",
            "Mechanical gaming keyboard with RGB", 30, "Accessories"));
        products.add(new Product(5, "Mouse", 25.00, "https://ik.imagekit.io/yfork8zzq/Copilot_20250628_113619.png?updatedAt=1751091027158",
            "Ergonomic wireless mouse", 40, "Accessories"));
        products.add(new Product(6, "Monitor", 300.00, "https://ik.imagekit.io/yfork8zzq/Copilot_20250628_113758.png?updatedAt=1751091022722",
            "27-inch 4K monitor with HDR", 8, "Electronics"));
    }

    private static void setupRoutes() {
        // Use PORT environment variable provided by Render, default to 8080 if not set
        port(Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")));
        
        // Configure static files to be served from the frontend directory
        staticFiles.externalLocation(System.getProperty("user.dir") + "/frontend");
        
        // Add root route to serve index.html
        get("/", (req, res) -> {
            res.redirect("/index.html");
            return null;
        });

        // Add endpoint to get CSRF token
        get("/csrf-token", (req, res) -> {
            res.type("application/json");
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", req.session(true).attribute(CSRF_TOKEN_KEY));
            return new Gson().toJson(tokenMap);
        });

        before((req, res) -> {
            if (req.session(true).attribute(CSRF_TOKEN_KEY) == null) {
                req.session().attribute(CSRF_TOKEN_KEY, UUID.randomUUID().toString());
            }
        });

        // Product routes
        get("/products", (req, res) -> {
            res.type("application/json");
            return new Gson().toJson(products);
        });

        get("/products/category/:category", (req, res) -> {
            String category = req.params(":category");
            List<Product> filteredProducts = products.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .collect(java.util.stream.Collectors.toList());
            res.type("application/json");
            return new Gson().toJson(filteredProducts);
        });

        // Cart routes
        get("/cart", (req, res) -> {
            res.type("application/json");
            return new Gson().toJson(CartService.getCart(req.session()));
        });

        post("/cart/add", (req, res) -> {
            validateCsrfToken(req);
            Product product = new Gson().fromJson(req.body(), Product.class);
            CartService.addProduct(req.session(), product, 1);
            return new Gson().toJson(CartService.getCart(req.session()));
        });

        post("/cart/update/:id", (req, res) -> {
            validateCsrfToken(req);
            int productId = Integer.parseInt(req.params(":id"));
            int quantity = Integer.parseInt(req.queryParams("quantity"));
            CartService.updateQuantity(req.session(), productId, quantity);
            return new Gson().toJson(CartService.getCart(req.session()));
        });

        post("/cart/remove/:id", (req, res) -> {
            validateCsrfToken(req);
            int productId = Integer.parseInt(req.params(":id"));
            CartService.removeProduct(req.session(), productId);
            return new Gson().toJson(CartService.getCart(req.session()));
        });

        // Checkout route
        post("/checkout", (req, res) -> {
            validateCsrfToken(req);
            Session session = req.session();
            List<Map<String, Object>> cart = CartService.getCart(session);

            if (cart.isEmpty()) {
                res.status(400);
                return "Cart is empty";
            }

            String name = req.queryParams("name");
            String email = req.queryParams("email");
            String address = req.queryParams("address");

            if (!isValidInput(name, email, address)) {
                res.status(400);
                return "Please fill out all fields correctly";
            }

            // Process order
            processOrder(name, email, address, cart);

            // Clear cart
            session.removeAttribute("cart");

            res.redirect("/confirmation.html");
            return "Order processed successfully!";
        });

        // Error handling
        exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body("Internal Server Error: " + e.getMessage());
        });
    }

    private static void validateCsrfToken(Request req) {
        String token = req.headers("X-CSRF-Token");
        String sessionToken = req.session().attribute(CSRF_TOKEN_KEY);
        
        if (token == null || !token.equals(sessionToken)) {
            halt(403, "Invalid CSRF token");
        }
    }

    private static boolean isValidInput(String name, String email, String address) {
        return name != null && !name.trim().isEmpty() &&
               email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$") &&
               address != null && !address.trim().isEmpty();
    }

    private static void processOrder(String name, String email, String address, List<Map<String, Object>> cart) {
        // Update inventory
        updateInventory(cart);
        
        // Send confirmation emails
        EmailService.sendOrderConfirmation(name, email, address, cart);
        EmailService.sendCustomerConfirmation(name, email, cart);
    }

    private static void updateInventory(List<Map<String, Object>> cart) {
        for (Map<String, Object> item : cart) {
            Product product = (Product) item.get("product");
            int quantity = ((Number) item.get("quantity")).intValue();
            
            products.stream()
                .filter(p -> p.getId() == product.getId())
                .findFirst()
                .ifPresent(p -> p.setQuantity(p.getQuantity() - quantity));
        }
    }
}
