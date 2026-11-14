package com.snapandpack.service;

import com.snapandpack.dto.ProductDtos.ProductRequest;
import com.snapandpack.dto.ProductDtos.ProductResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.model.Product;
import com.snapandpack.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Listing products for shoppers and managing them for staff. */
@Service
public class ProductService {

    private final ProductRepository products;
    private final AuditService audit;

    /** Receives database access for products and the activity record. */
    public ProductService(ProductRepository products, AuditService audit) {
        this.products = products;
        this.audit = audit;
    }

    /** Every product, for the store page and the admin table. */
    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return products.findAllByOrderByIdAsc().stream().map(ProductResponse::from).toList();
    }

    /** One product by its id. */
    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    /** Adds a product on behalf of a member of staff, and records it. */
    @Transactional
    public ProductResponse create(ProductRequest request) {
        ProductResponse created = seed(request);
        audit.record("PRODUCT_CREATED", created.name());
        return created;
    }

    /** Adds a product without an activity line. Used when loading the starting products. */
    @Transactional
    public ProductResponse seed(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return ProductResponse.from(products.save(product));
    }

    /** Replaces a product's details with those from the form, and records it. */
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        apply(product, request);
        audit.record("PRODUCT_UPDATED", product.getName());
        return ProductResponse.from(product);
    }

    /** Removes a product. Past orders keep their own copy of its name and price. */
    @Transactional
    public void delete(Long id) {
        Product product = find(id);
        products.delete(product);
        audit.record("PRODUCT_DELETED", product.getName());
    }

    /** Loads a product or fails with "not found". */
    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> ApiException.notFound("Product not found"));
    }

    /** Copies the form's values onto a product, tidying the colour and size lists. */
    private static void apply(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setCategory(request.category());
        product.setGender(request.gender());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImage(request.image() == null || request.image().isBlank() ? null : request.image().trim());
        product.getColors().clear();
        product.getColors().addAll(clean(request.colors()));
        product.getSizes().clear();
        product.getSizes().addAll(clean(request.sizes()));
    }

    /** Trims each value and drops blanks and repeats. */
    private static List<String> clean(List<String> values) {
        return values.stream().map(String::trim).filter(v -> !v.isEmpty()).distinct().toList();
    }
}
