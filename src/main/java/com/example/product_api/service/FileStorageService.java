package com.example.product_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    // Types d'images autorisés
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    // Taille max : 100 MB
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossible de créer le dossier d'upload", ex);
        }
    }

    //  Stocker un fichier
    public String storeFile(MultipartFile file, Long productId) {
        // Valider le fichier
        validateFile(file);

        // Nettoyer le nom du fichier
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Générer un nom unique
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(originalFilename);
        String filename = "image_" + timestamp + "_" + System.nanoTime() + extension;

        try {
            // Créer le dossier du produit
            Path productDir = this.fileStorageLocation.resolve("products").resolve(productId.toString());
            Files.createDirectories(productDir);

            // Chemin complet du fichier
            Path targetLocation = productDir.resolve(filename);

            // Copier le fichier
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Retourner le chemin relatif
            return "products/" + productId + "/" + filename;

        } catch (IOException ex) {
            throw new RuntimeException("Erreur lors du stockage du fichier " + filename, ex);
        }
    }

    //  Charger un fichier
    public Resource loadFileAsResource(String filepath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filepath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier non trouvé : " + filepath);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Fichier non trouvé : " + filepath, ex);
        }
    }

    // Supprimer un fichier
    public void deleteFile(String filepath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filepath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Erreur lors de la suppression du fichier", ex);
        }
    }

    // Valider le fichier
    private void validateFile(MultipartFile file) {
        // Vérifier si le fichier est vide
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        // Vérifier le type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Type de fichier non autorisé. Types acceptés : JPEG, PNG, GIF, WebP");
        }

        // Vérifier la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Le fichier est trop volumineux. Taille max : 100 MB");
        }
    }

    // Obtenir l'extension du fichier
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}