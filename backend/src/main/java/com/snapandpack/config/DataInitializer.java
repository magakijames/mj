package com.snapandpack.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapandpack.dto.ProductDtos.ProductRequest;
import com.snapandpack.model.Role;
import com.snapandpack.model.User;
import com.snapandpack.repository.ProductRepository;
import com.snapandpack.repository.UserRepository;
import com.snapandpack.service.ProductService;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once at startup. Creates the first admin if there is none,
 * and loads the starting products (resources/seed/products.json) if the products table is empty.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository users;
    private final ProductRepository products;
    private final ProductService productService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${app.admin.name}")
    private String adminName;
    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UserRepository users, ProductRepository products, ProductService productService,
                           PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.users = users;
        this.products = products;
        this.productService = productService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!users.existsByRole(Role.ADMIN)) {
            User admin = new User();
            admin.setName(adminName);
            admin.setEmail(adminEmail.trim().toLowerCase());
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
            users.save(admin);
            log.info("Created the first admin account: {}", admin.getEmail());
        }

        if (products.count() == 0) {
            try (InputStream in = new ClassPathResource("seed/products.json").getInputStream()) {
                List<ProductRequest> seed = objectMapper.readValue(in, new TypeReference<>() { });
                seed.forEach(productService::create);
                log.info("Loaded {} starting products", seed.size());
            }
        }
    }
}
