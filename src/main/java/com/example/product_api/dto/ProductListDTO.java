package com.example.product_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private Boolean active;
    private Boolean inStockAlert;
}
