package com.example.product_api.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageContext;
import org.hibernate.grammars.hql.HqlParser;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ProductRequestDTO {

    @NotBlank(message = "le nom est obligatoire")
    @Size(min=3 , max =200, message = "le nom doit contenier entre 3 et 200 caractere")
    private String name;


    @Size (max = 1000,message = "la descriptionne peut pas depaser 1000 caracter")
    private String description;

    @NotNull(message = "le prix est obligatoire")
    @DecimalMin(value = "0.01",message = "le prix doit etre superieur a 0")
    @Digits(integer = 10,fraction = 2,message = "format de prix invalide")
    private BigDecimal price;


    @NotNull(message = "le stock est obligatoire")
    @Min(value = 0,message = "le stock ne peut pas etre negatif")
    private Integer stock;


    @Min(value = 0,message = "le seuil d'alerte ne pes etre negatif")
    private BigDecimal alertThreshold;

    @Size(max = 100,message = "la cotegorie ne peut pas depasser 100 caracteres")
    private String category;

    @Pattern(regexp = "^[A-Z0-9-]*$",message = "le sku ne peut conternir que des majiscules ,chiffres et titrets")
    @Size(max = 50,message = "le sku ne peut depaser 50 caracter")
    private String sku;

    private Boolean active=true;




}
