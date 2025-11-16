package com.example.product_api.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDTO {

    private Long id;
    private String filename;
    private String url;              // URL pour accéder à l'image
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}