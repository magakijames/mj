package com.snapandpack.service;

import com.snapandpack.dto.ProductDtos.ProductRequest;
import com.snapandpack.dto.ProductDtos.ProductResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.model.Product;
import com.snapandpack.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return products.findAllByOrderByIdAsc().stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return ProductResponse.from(products.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        apply(product, request);
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        products.delete(find(id));
    }

    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> ApiException.notFound("Product not found"));
    }

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

    private static List<String> clean(List<String> values) {
        return values.stream().map(String::trim).filter(v -> !v.isEmpty()).distinct().toList();
    }
}
