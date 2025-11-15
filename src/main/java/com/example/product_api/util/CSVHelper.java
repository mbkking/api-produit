package com.example.product_api.util;

import com.example.productapi.entity.Product;
import org.apache.commons.csv.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVHelper {

    public static String TYPE = "text/csv";

    // Headers du fichier CSV
    static String[] HEADERS = {
            "ID", "Name", "Description", "Price", "Stock",
            "AlertThreshold", "Category", "SKU", "Active"
    };

    // ========== Vérifier si le fichier est un CSV ==========
    public static boolean hasCSVFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType()) ||
                file.getOriginalFilename().endsWith(".csv");
    }

    // ========== IMPORT : Lire un CSV et créer des Products ==========
    public static List<Product> csvToProducts(InputStream inputStream) {
        try (BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.builder()
                             .setHeader(HEADERS)
                             .setSkipHeaderRecord(true)
                             .setIgnoreHeaderCase(true)
                             .setTrim(true)
                             .build())) {

            List<Product> products = new ArrayList<>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                Product product = Product.builder()
                        .name(csvRecord.get("Name"))
                        .description(csvRecord.get("Description"))
                        .price(new BigDecimal(csvRecord.get("Price")))
                        .stock(Integer.parseInt(csvRecord.get("Stock")))
                        .alertThreshold(parseInteger(csvRecord.get("AlertThreshold")))
                        .category(csvRecord.get("Category"))
                        .sku(csvRecord.get("SKU"))
                        .active(parseBoolean(csvRecord.get("Active")))
                        .build();

                products.add(product);
            }

            return products;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
    }

    // ========== EXPORT : Créer un CSV depuis des Products ==========
    public static ByteArrayInputStream productsToCSV(List<Product> products) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(
                     new PrintWriter(out, true, StandardCharsets.UTF_8),
                     CSVFormat.DEFAULT.builder()
                             .setHeader(HEADERS)
                             .build())) {

            for (Product product : products) {
                csvPrinter.printRecord(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStock(),
                        product.getAlertThreshold(),
                        product.getCategory(),
                        product.getSku(),
                        product.getActive()
                );
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la création du fichier CSV: " + e.getMessage());
        }
    }

    // ========== Méthodes utilitaires ==========

    private static Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Valeur par défaut
        }
        return Boolean.parseBoolean(value.trim());
    }
}