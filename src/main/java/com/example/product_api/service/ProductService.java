package com.example.product_api.service;

import com.example.product_api.dto.PageResponse;
import com.example.product_api.dto.ProductListDTO;
import com.example.product_api.dto.ProductRequestDTO;
import com.example.product_api.dto.ProductResponseDTO;
import com.example.product_api.entity.Product;
import com.example.product_api.execption.ResourceNotFoundException;
import com.example.product_api.execption.ResourceAlreadyExistsException;

import com.example.product_api.mapper.ProductMapper;
import com.example.product_api.repositoty.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service                    // ← Spring gère cette classe comme un service
@RequiredArgsConstructor    // ← Lombok crée le constructeur avec les final
@Transactional
// Une transaction = un ensemble d'opérations qui doivent
// toutes réussir ensemble ou toutes échouer ensemble.
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // ========== CREATE (inchangé) ==========
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        if (requestDTO.getSku() != null &&
                productRepository.findBySku(requestDTO.getSku()).isPresent()) {
            throw new ResourceAlreadyExistsException("Product", "SKU", requestDTO.getSku());
        }

        Product product = productMapper.toEntity(requestDTO);
        Product savedProduct = productRepository.save(product);

        return productMapper.toResponseDTO(savedProduct);
    }

    // ========== READ - Un seul produit (inchangé) ==========
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        return productMapper.toResponseDTO(product);
    }

    // ========== READ - Tous les produits SANS pagination (existant) ==========
    @Transactional(readOnly = true)
    public List<ProductListDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }

    // ========== NOUVEAU : READ - Tous les produits AVEC pagination ==========
    @Transactional(readOnly = true)
    public PageResponse<ProductListDTO> getAllProductsPaginated(
            int page,
            int size,
            String sortBy,
            String direction) {

        // Créer l'objet Pageable avec tri
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Récupérer la page de produits
        Page<Product> productPage = productRepository.findAll(pageable);

        // Convertir Product → ProductListDTO
        Page<ProductListDTO> dtoPage = productPage.map(productMapper::toListDTO);

        // Créer la réponse paginée
        return new PageResponse<>(dtoPage);
    }

    // ========== UPDATE (inchangé) ==========
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO requestDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (requestDTO.getSku() != null) {
            productRepository.findBySku(requestDTO.getSku())
                    .ifPresent(product -> {
                        if (!product.getId().equals(id)) {
                            throw new ResourceAlreadyExistsException("Product", "SKU", requestDTO.getSku());
                        }
                    });
        }

        productMapper.updateEntityFromDto(requestDTO, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);

        return productMapper.toResponseDTO(updatedProduct);
    }

    // ========== DELETE (inchangé) ==========
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        productRepository.deleteById(id);
    }

    // ========== RECHERCHE AVANCÉE SANS pagination (existant) ==========
    @Transactional(readOnly = true)
    public List<ProductListDTO> searchProducts(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword) {

        List<Product> products = productRepository.searchProducts(
                category, minPrice, maxPrice, keyword
        );

        return products.stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }

    // ========== NOUVEAU : RECHERCHE AVANCÉE AVEC pagination ==========
    @Transactional(readOnly = true)
    public PageResponse<ProductListDTO> searchProductsPaginated(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.searchProducts(
                category, minPrice, maxPrice, keyword, pageable
        );

        Page<ProductListDTO> dtoPage = productPage.map(productMapper::toListDTO);

        return new PageResponse<>(dtoPage);
    }

    // ========== PRODUITS EN ALERTE STOCK (inchangé) ==========
    @Transactional(readOnly = true)
    public List<ProductListDTO> getProductsInStockAlert() {
        return productRepository.findProductsInStockAlert()
                .stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }

    // ========== RECHERCHE PAR CATÉGORIE SANS pagination (existant) ==========
    @Transactional(readOnly = true)
    public List<ProductListDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }

    // ========== NOUVEAU : RECHERCHE PAR CATÉGORIE AVEC pagination ==========
    @Transactional(readOnly = true)
    public PageResponse<ProductListDTO> getProductsByCategoryPaginated(
            String category,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        Page<ProductListDTO> dtoPage = productPage.map(productMapper::toListDTO);

        return new PageResponse<>(dtoPage);
    }

    // ========== RECHERCHE PAR NOM (inchangé) ==========
    @Transactional(readOnly = true)
    public List<ProductListDTO> searchByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }
}