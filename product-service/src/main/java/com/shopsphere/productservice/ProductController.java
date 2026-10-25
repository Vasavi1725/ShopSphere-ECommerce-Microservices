package com.shopsphere.productservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = {
        "http://localhost:63342",
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
@RestController
@RequestMapping("/products")
public class ProductController {


    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Get all products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // Get a product by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);

        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Product not found"));
        }

        return ResponseEntity.ok(product);
    }

    // Create a product
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        if (product == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Product data is required"));
        }

        Product savedProduct = productService.createProduct(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // Update a product
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct) {

        Product product = productService.updateProduct(id, updatedProduct);

        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Product not found or invalid data"));
        }

        return ResponseEntity.ok(product);
    }

    // Delete a product
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (productService.getProductById(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Product not found"));
        }

        productService.deleteProduct(id);

        return ResponseEntity.ok(
                Map.of("message", "Product deleted successfully"));
    }

    // Reduce stock
    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<?> reduceStock(
            @PathVariable Long id,
            @RequestParam int quantity) {

        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Quantity must be greater than zero"));
        }

        Product product = productService.reduceStock(id, quantity);

        if (product == null) {
            Product existingProduct = productService.getProductById(id);

            if (existingProduct == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Product not found"));
            }

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Insufficient stock available"));
        }

        return ResponseEntity.ok(product);
    }

    // Restore stock
    @PutMapping("/{id}/increase-stock")
    public ResponseEntity<?> increaseStock(
            @PathVariable Long id,
            @RequestParam int quantity) {

        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Quantity must be greater than zero"));
        }

        Product product = productService.increaseStock(id, quantity);

        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Product not found"));
        }

        return ResponseEntity.ok(product);
    }
}