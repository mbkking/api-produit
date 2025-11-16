package com.example.product_api.controller;

import com.example.product_api.entity.ProductImage;
import com.example.product_api.execption.ResourceNotFoundException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.example.product_api.dto.PageResponse;
import com.example.product_api.dto.ProductListDTO;
import com.example.product_api.dto.ProductRequestDTO;
import com.example.product_api.dto.ProductResponseDTO;
import com.example.product_api.service.ProductService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.product_api.repositoty.ProductImageRepository;
import com.example.product_api.dto.ProductImageDTO;
import com.example.product_api.service.FileStorageService;
import org.springframework.core.io.Resource;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final FileStorageService fileStorageService;
    private final ProductService productService;
    private final ProductImageRepository productImageRepository;

    //  CREATE
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductRequestDTO requestDTO) {

        ProductResponseDTO response = productService.createProduct(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //  READ -
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @PathVariable Long id) {

        ProductResponseDTO response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    // liste de tous les produts
    @GetMapping("/all")
    public ResponseEntity<List<ProductListDTO>> getAllProductsNoPagination() {
        List<ProductListDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    //liste de produit avec pagination
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

    //mise a jour
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO requestDTO) {

        ProductResponseDTO response = productService.updateProduct(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    //supprimer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    //recherche de produit sans pagination
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

    // recherche de produits avec  pagination
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

    // liste des produits en alert ALERTS
    @GetMapping("/alerts")
    public ResponseEntity<List<ProductListDTO>> getProductsInStockAlert() {
        List<ProductListDTO> products = productService.getProductsInStockAlert();
        return ResponseEntity.ok(products);
    }

    //  listes de categorie sans  pagination
    @GetMapping("/category/{category}/simple")
    public ResponseEntity<List<ProductListDTO>> getProductsByCategorySimple(
            @PathVariable String category) {

        List<ProductListDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    // liste AVEC pagination
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

    // recherche par nom
    @GetMapping("/search/name")
    public ResponseEntity<List<ProductListDTO>> searchByName(
            @RequestParam String keyword) {

        List<ProductListDTO> products = productService.searchByName(keyword);
        return ResponseEntity.ok(products);
    }
     @PostMapping("/import")
    public ResponseEntity<?> importProducts(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Fichier vide");
        }

        try {
            int count = productService.importProductsFromCSV(file);

            return ResponseEntity.ok()
                    .body(new ImportResponse(
                            "Import réussi",
                            count + " produits importés",
                            count
                    ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImportResponse(
                            "Erreur lors de l'import",
                            e.getMessage(),
                            0
                    ));
        }
    }

    //  EXPORT CSV
    @GetMapping("/export")
    public ResponseEntity<Resource> exportProducts() {

        String filename = "products_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".csv";

        ByteArrayInputStream data = productService.exportProductsToCSV();
        InputStreamResource resource = new InputStreamResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    //  EXPORT CSV avec filtres
    @GetMapping("/export/filtered")
    public ResponseEntity<Resource> exportProductsFiltered(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword) {

        String filename = "products_filtered_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".csv";

        ByteArrayInputStream data = productService.exportProductsToCSVFiltered(
                category, minPrice, maxPrice, keyword
        );
        InputStreamResource resource = new InputStreamResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    //  la réponse d'import
    @lombok.Data
    @lombok.AllArgsConstructor
    static class ImportResponse {
        private String status;
        private String message;
        private int count;
    }


    // UPLOAD IMAGES
    @PostMapping("/{id}/images")
    public ResponseEntity<List<ProductImageDTO>> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] files) {

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().build();
        }

        List<ProductImageDTO> uploadedImages = productService.uploadImages(id, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedImages);
    }

    //  GET IMAGES d'un produit
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ProductImageDTO>> getProductImages(@PathVariable Long id) {
        List<ProductImageDTO> images = productService.getProductImages(id);
        return ResponseEntity.ok(images);
    }

    //  GET une image
    @GetMapping("/{productId}/images/{imageId}")
    public ResponseEntity<Resource> getImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        // Récupérer l'info de l'image en BD
        ProductImage image = productService.getProductImages(productId).stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .map(dto -> {
                    // Récupérer l'entité depuis la BD
                    return productImageRepository.findById(imageId)
                            .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));
                })
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));

        // Charger le fichier
        Resource resource = fileStorageService.loadFileAsResource(image.getFilepath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFilename() + "\"")
                .body(resource);
    }

    // supprimer une image
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        productService.deleteImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }
}