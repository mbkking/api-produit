package com.example.product_api.mapper;

import com.example.product_api.dto.ProductListDTO;
import com.example.product_api.dto.ProductRequestDTO;
import com.example.product_api.dto.ProductResponseDTO;
import com.example.product_api.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .alertThreshold(dto.getAlertThreshold())
                .category(dto.getCategory())
                .sku(dto.getSku())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }

    public void updateEntityFromDto(ProductRequestDTO dto, Product entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setAlertThreshold(dto.getAlertThreshold());
        entity.setCategory(dto.getCategory());
        entity.setSku(dto.getSku());

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }

    public ProductResponseDTO toResponseDTO(Product entity) {
        if (entity == null) {
            return null;
        }

        boolean inAlert = false;

        String stockStatus;
        if (entity.getStock() == 0) {
            stockStatus = "Rupture de stock";
        } else if (inAlert) {
            stockStatus = "Stock faible";
        } else {
            stockStatus = "En stock";
        }

        return ProductResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .alertThreshold(entity.getAlertThreshold())  // ✅ Corrigé ici
                .category(entity.getCategory())
                .sku(entity.getSku())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .inStockAlert(inAlert)
                .stockStatus(stockStatus)
                .build();
    }

    public ProductListDTO toListDTO(Product entity) {
        if (entity == null) {
            return null;
        }

        boolean inAlert = false;

        return ProductListDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .category(entity.getCategory())
                .active(entity.getActive())
                .inStockAlert(inAlert)
                .build();
    }
}