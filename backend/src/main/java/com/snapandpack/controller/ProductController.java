package com.snapandpack.controller;

import com.snapandpack.dto.ProductDtos.ProductRequest;
import com.snapandpack.dto.ProductDtos.ProductResponse;
import com.snapandpack.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Addresses for products (/api/products). Anyone can look; only the admin and product managers can change. */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final String CAN_EDIT = "hasAnyRole('ADMIN', 'PRODUCT_MANAGER')";

    private final ProductService productService;

    /** Receives the service this controller passes requests to. */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** GET /api/products: every product. Open to everyone. */
    @GetMapping
    public List<ProductResponse> list() {
        return productService.list();
    }

    /** GET /api/products/{id}: one product. Open to everyone. */
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.get(id);
    }

    /** POST /api/products: adds a product. Admin and product managers only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(CAN_EDIT)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    /** PUT /api/products/{id}: replaces a product's details. Admin and product managers only. */
    @PutMapping("/{id}")
    @PreAuthorize(CAN_EDIT)
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    /** DELETE /api/products/{id}: removes a product. Admin and product managers only. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(CAN_EDIT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
