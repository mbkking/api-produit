package com.example.product_api.repositoty;


import com.example.product_api.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Trouver toutes les images d'un produit
    List<ProductImage> findByProductId(Long productId);

    // Compter les images d'un produit
    Long countByProductId(Long productId);
}