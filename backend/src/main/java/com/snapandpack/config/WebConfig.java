package com.snapandpack.config;

import com.snapandpack.service.UploadService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Web settings that are not about security. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UploadService uploadService;

    /** Receives the upload service, which knows where uploaded photos are kept. */
    public WebConfig(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /** Serves uploaded product photos from their folder at the address /uploads/... */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadService.folder().toUri().toString());
    }
}
