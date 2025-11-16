package com.example.product_api.service;

import com.example.product_api.dto.PageResponse;
import com.example.product_api.dto.ProductListDTO;
import com.example.product_api.dto.ProductRequestDTO;
import com.example.product_api.dto.ProductResponseDTO;
import com.example.product_api.entity.Product;
import com.example.product_api.execption.ResourceNotFoundException;
import com.example.product_api.execption.ResourceAlreadyExistsException;
import com.example.product_api.util.CSVHelper;
import com.example.product_api.dto.ProductImageDTO;
import com.example.product_api.entity.ProductImage;
import com.example.product_api.repositoty.ProductImageRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
// Une transaction = un ensemble d'opérations qui doivent
// toutes réussir ensemble ou toutes échouer ensemble.
public class ProductService {

    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    //  CREATE (inchangé)
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        if (requestDTO.getSku() != null &&
                productRepository.findBySku(requestDTO.getSku()).isPresent()) {
            throw new ResourceAlreadyExistsException("Product", "SKU", requestDTO.getSku());
        }

        Product product = productMapper.toEntity(requestDTO);
        Product savedProduct = productRepository.save(product);

        return productMapper.toResponseDTO(savedProduct);
    }

    // Un seul produit (inchangé)
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        return productMapper.toResponseDTO(product);
    }

    //  READ - Tous les produits SANS pagination (existant)
    @Transactional(readOnly = true)
    public List<ProductListDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }

    //  READ - Tous les produits AVEC pagination
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

    //  UPDATE (inchangé)
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

    //  DELETE
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        productRepository.deleteById(id);
    }

    //  RECHERCHE AVANCÉE SANS pagination
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

    //  RECHERCHE AVANCÉE AVEC pagination
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

    // PRODUITS EN ALERTE STOCK (
    @Transactional(readOnly = true)
    public List<ProductListDTO> getProductsInStockAlert() {
        return productRepository.findProductsInStockAlert()
                .stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }

    //  RECHERCHE PAR CATÉGORIE SANS pagination
    @Transactional(readOnly = true)
    public List<ProductListDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }

    //  RECHERCHE PAR CATÉGORIE AVEC pagination
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

    //  RECHERCHE PAR NOM
    @Transactional(readOnly = true)
    public List<ProductListDTO> searchByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }
    public int importProductsFromCSV(MultipartFile file) {
        // Vérifier le format
        if (!CSVHelper.hasCSVFormat(file)) {
            throw new RuntimeException("Le fichier doit être au format CSV");
        }

        try {
            // Lire le CSV et créer les produits
            List<Product> products = CSVHelper.csvToProducts(file.getInputStream());

            // Sauvegarder tous les produits en batch
            List<Product> savedProducts = productRepository.saveAll(products);

            return savedProducts.size();

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'import du fichier CSV: " + e.getMessage());
        }
    }

    //  EXPORT CSV
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportProductsToCSV() {
        List<Product> products = productRepository.findAll();
        return CSVHelper.productsToCSV(products);
    }

    // EXPORT CSV avec filtres
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportProductsToCSVFiltered(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword) {

        List<Product> products = productRepository.searchProducts(
                category, minPrice, maxPrice, keyword
        );

        return CSVHelper.productsToCSV(products);
    }


    //  uPLOAD IMAGES
    public List<ProductImageDTO> uploadImages(Long productId, MultipartFile[] files) {
        // Vérifier que le produit existe
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        List<ProductImageDTO> uploadedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            // Stocker le fichier
            String filepath = fileStorageService.storeFile(file, productId);

            // Créer l'entité ProductImage
            ProductImage productImage = ProductImage.builder()
                    .filename(file.getOriginalFilename())
                    .filepath(filepath)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .product(product)
                    .build();

            // Sauvegarder en BD
            ProductImage saved = productImageRepository.save(productImage);

            // Créer le DTO de réponse
            ProductImageDTO dto = convertToImageDTO(saved);
            uploadedImages.add(dto);
        }

        return uploadedImages;
    }

    // GET IMAGES d'un produit
    @Transactional(readOnly = true)
    public List<ProductImageDTO> getProductImages(Long productId) {
        // Vérifier que le produit existe
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        List<ProductImage> images = productImageRepository.findByProductId(productId);

        return images.stream()
                .map(this::convertToImageDTO)
                .collect(Collectors.toList());
    }

    // supprimer une image
    public void deleteImage(Long productId, Long imageId) {
        // Vérifier que le produit existe
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        // Récupérer l'image
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));

        // Vérifier que l'image appartient bien à ce produit
        if (!image.getProduct().getId().equals(productId)) {
            throw new RuntimeException("L'image n'appartient pas à ce produit");
        }

        // Supprimer le fichier physique
        fileStorageService.deleteFile(image.getFilepath());

        // Supprimer l'entrée en BD
        productImageRepository.delete(image);
    }

    //  Convertir ProductImage → ProductImageDTO
    private ProductImageDTO convertToImageDTO(ProductImage image) {
        // Construire l'URL complète pour accéder à l'image
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/products/")
                .path(image.getProduct().getId().toString())
                .path("/images/")
                .path(image.getId().toString())
                .toUriString();

        return ProductImageDTO.builder()
                .id(image.getId())
                .filename(image.getFilename())
                .url(imageUrl)
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .uploadedAt(image.getUploadedAt())
                .build();
    }
}