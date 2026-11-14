package com.snapandpack.controller;

import com.snapandpack.service.SettingsService;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Addresses for the editable site text (/api/settings). */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    /** Receives the service this controller passes requests to. */
    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /** GET /api/settings: the shop name and other site text. Open to everyone, since every page shows it. */
    @GetMapping
    public Map<String, String> get() {
        return settingsService.get();
    }

    /** PUT /api/settings: saves changed site text and returns the full list. Admin only. */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> update(@RequestBody Map<String, String> changes) {
        return settingsService.update(changes);
    }
}
