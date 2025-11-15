package com.example.product_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;           // Les données de la page
    private int pageNumber;            // Numéro de page actuelle (commence à 0)
    private int pageSize;              // Taille de la page (nombre d'éléments)
    private long totalElements;        // Nombre total d'éléments
    private int totalPages;            // Nombre total de pages
    private boolean first;             // Est-ce la première page ?
    private boolean last;              // Est-ce la dernière page ?
    private boolean empty;             // La page est-elle vide ?

    // Constructeur pratique à partir d'une Page Spring
    public PageResponse(org.springframework.data.domain.Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.empty = page.isEmpty();
    }

}
