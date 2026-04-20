package com.example.juniorinterview.controller;

import com.example.juniorinterview.dto.CursorResponse;
import com.example.juniorinterview.exception.BadRequestException;
import com.example.juniorinterview.model.Product;
import com.example.juniorinterview.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ProductController.BASE_PATH)
public class ProductController {

    static final String BASE_PATH = "/api/products";
    private static final String ID_PATH = "/{id:\\d+}";
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/cursor")
    public ResponseEntity<CursorResponse<Product>> getProductsCursor(
            @RequestParam(name = "after", required = false) Long after,
            @RequestParam(name = "limit", defaultValue = "" + DEFAULT_LIMIT) int limit) {

        validateCursorParams(after, limit);

        return ResponseEntity.ok(productService.findByCursor(after, limit));
    }

    @GetMapping(ID_PATH)
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product created = productService.create(product);
        URI location = URI.create(BASE_PATH + "/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping(ID_PATH)
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.update(id, product));
    }

    @PatchMapping(ID_PATH)
    public ResponseEntity<Product> patchProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> changes) {
        return ResponseEntity.ok(productService.patch(id, changes));
    }

    @DeleteMapping(ID_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }

    private void validateCursorParams(Long after, int limit) {
        if (after != null && after < 0) {
            throw new BadRequestException("Query parameter 'after' must be greater than or equal to 0");
        }

        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new BadRequestException("Query parameter 'limit' must be between 1 and " + MAX_LIMIT);
        }
    }
}
