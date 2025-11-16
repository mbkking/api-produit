package com.example.product_api.repositoty;

import com.example.product_api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    //  MÉTHODES EXISTANTES sans pagination)

    Optional<Product> findByName(String name);
    Optional<Product> findBySku(String sku);
    List<Product> findByActiveTrue();
    List<Product> findByCategory(String category);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT p FROM Product p WHERE p.stock <= p.alertThreshold")
    List<Product> findProductsInStockAlert();

    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchProducts(
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword
    );

    Long countByCategory(String category);

    //  NOUVELLES MÉTHODES PAGINÉES

    // Tous les produits avec pagination
     Page<Product> findAll(Pageable pageable);

    // Par catégorie avec pagination
    Page<Product> findByCategory(String category, Pageable pageable);

    // Produits actifs avec pagination
    Page<Product> findByActiveTrue(Pageable pageable);

    // Recherche par nom avec pagination
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // Recherche avancée avec pagination
    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}