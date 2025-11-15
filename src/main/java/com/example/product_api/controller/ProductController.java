package com.example.product_api.controller;


import com.example.product_api.dto.PageResponse;
import com.example.product_api.dto.ProductListDTO;
import com.example.product_api.dto.ProductRequestDTO;
import com.example.product_api.dto.ProductResponseDTO;
import com.example.product_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController                           // ← Indique que c'est un controller REST
@RequestMapping("/api/v1/products")       // ← Préfixe pour toutes les routes
@RequiredArgsConstructor                  // ← Injection automatique
public class ProductController {
    private final ProductService productService;

    // ========== CREATE (inchangé) ==========
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductRequestDTO requestDTO) {

        ProductResponseDTO response = productService.createProduct(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== READ - Un produit (inchangé) ==========
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @PathVariable Long id) {

        ProductResponseDTO response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    // ========== READ - Tous SANS pagination (existant, garde pour compatibilité) ==========
    @GetMapping("/all")
    public ResponseEntity<List<ProductListDTO>> getAllProductsNoPagination() {
        List<ProductListDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // ========== NOUVEAU : READ - Tous AVEC pagination (endpoint principal) ==========
    @GetMapping
    public ResponseEntity<PageResponse<ProductListDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        PageResponse<ProductListDTO> response = productService.getAllProductsPaginated(
                page, size, sortBy, direction
        );
        return ResponseEntity.ok(response);
    }

    // ========== UPDATE (inchangé) ==========
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO requestDTO) {

        ProductResponseDTO response = productService.updateProduct(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    // ========== DELETE (inchangé) ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ========== SEARCH SANS pagination (garde pour compatibilité) ==========
    @GetMapping("/search/simple")
    public ResponseEntity<List<ProductListDTO>> searchProductsSimple(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword) {

        List<ProductListDTO> products = productService.searchProducts(
                category, minPrice, maxPrice, keyword
        );
        return ResponseEntity.ok(products);
    }

    // ========== NOUVEAU : SEARCH AVEC pagination ==========
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductListDTO>> searchProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        PageResponse<ProductListDTO> response = productService.searchProductsPaginated(
                category, minPrice, maxPrice, keyword, page, size, sortBy, direction
        );
        return ResponseEntity.ok(response);
    }

    // ========== ALERTS (inchangé) ==========
    @GetMapping("/alerts")
    public ResponseEntity<List<ProductListDTO>> getProductsInStockAlert() {
        List<ProductListDTO> products = productService.getProductsInStockAlert();
        return ResponseEntity.ok(products);
    }

    // ========== BY CATEGORY SANS pagination (garde) ==========
    @GetMapping("/category/{category}/simple")
    public ResponseEntity<List<ProductListDTO>> getProductsByCategorySimple(
            @PathVariable String category) {

        List<ProductListDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    // ========== NOUVEAU : BY CATEGORY AVEC pagination ==========
    @GetMapping("/category/{category}")
    public ResponseEntity<PageResponse<ProductListDTO>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        PageResponse<ProductListDTO> response = productService.getProductsByCategoryPaginated(
                category, page, size, sortBy, direction
        );
        return ResponseEntity.ok(response);
    }

    // ========== SEARCH BY NAME (inchangé) ==========
    @GetMapping("/search/name")
    public ResponseEntity<List<ProductListDTO>> searchByName(
            @RequestParam String keyword) {

        List<ProductListDTO> products = productService.searchByName(keyword);
        return ResponseEntity.ok(products);
    }
}