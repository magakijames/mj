package com.snapandpack.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snapandpack.dto.ProductDtos.ProductRequest;
import com.snapandpack.model.Role;
import com.snapandpack.repository.ProductRepository;
import com.snapandpack.repository.UserRepository;
import com.snapandpack.service.ProductService;
import com.snapandpack.service.UserService;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
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
    private final UserService userService;
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @Value("${app.admin.name}")
    private String adminName;
    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.password}")
    private String adminPassword;

    /** Receives the services and database access this class needs. */
    public DataInitializer(UserRepository users, ProductRepository products, UserService userService,
                           ProductService productService, ObjectMapper objectMapper) {
        this.users = users;
        this.products = products;
        this.userService = userService;
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    /** Called by Spring when the server has started: fills in the first admin and the starting products. */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        createFirstAdmin();
        loadStartingProducts();
    }

    /** Creates the admin account from application.properties, but only when no admin exists yet. */
    private void createFirstAdmin() {
        if (users.existsByRole(Role.ADMIN)) {
            return;
        }
        userService.createAccount(adminName, adminEmail, adminPassword, Role.ADMIN);
        log.info("Created the first admin account: {}", adminEmail.trim().toLowerCase());
    }

    /** Reads seed/products.json into the products table, but only when the table is empty. */
    private void loadStartingProducts() throws Exception {
        if (products.count() > 0) {
            return;
        }
        try (InputStream in = new ClassPathResource("seed/products.json").getInputStream()) {
            List<ProductRequest> seed = objectMapper.readValue(in, new TypeReference<>() { });
            seed.forEach(productService::seed);
            log.info("Loaded {} starting products", seed.size());
        }
    }
}
