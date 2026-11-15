package com.snapandpack.controller;

import com.snapandpack.dto.UploadResponse;
import com.snapandpack.security.Access;
import com.snapandpack.service.UploadService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Address for uploading product photos (/api/uploads). */
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;

    /** Receives the service this controller passes requests to. */
    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /** POST /api/uploads/product-image: checks and saves a JPEG or PNG photo. Staff who manage products only. */
    @PostMapping("/product-image")
    @PreAuthorize(Access.PRODUCT_EDITORS)
    public UploadResponse productImage(@RequestParam("file") MultipartFile file) {
        return new UploadResponse(uploadService.saveProductImage(file));
    }
}
