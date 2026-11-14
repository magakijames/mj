package com.snapandpack.service;

import com.snapandpack.exception.ApiException;
import com.snapandpack.model.SiteSetting;
import com.snapandpack.repository.SiteSettingRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The site text the admin can edit: shop name, announcement line, home page headline, the four
 * promise boxes (shipping, returns, support, checkout), footer text and contact details.
 * Only the keys listed in DEFAULTS exist; a setting the admin has never changed uses its default.
 */
@Service
public class SettingsService {

    private static final int MAX_LENGTH = 500;

    /** Every setting the site understands, with the text used until the admin changes it. */
    private static final Map<String, String> DEFAULTS;

    static {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("shop_name", "Snap & Pack");
        d.put("tagline", "Bags & caps for every day");
        d.put("announcement", "Free shipping on orders over KSh 6,500 \u00b7 Easy 30-day returns");
        d.put("hero_title", "Carry it well.\nWear it better.");
        d.put("hero_text", "Backpacks, handbags and caps in the sizes and colours you actually want.");
        d.put("perk1_title", "Free shipping");
        d.put("perk1_text", "On orders over KSh 6,500");
        d.put("perk2_title", "Easy returns");
        d.put("perk2_text", "30 days, no fuss");
        d.put("perk3_title", "Customer support");
        d.put("perk3_text", "Here every day");
        d.put("perk4_title", "Secure checkout");
        d.put("perk4_text", "Your details stay safe");
        d.put("footer_about", "Your one-stop shop for bags and caps.");
        d.put("contact_email", "");
        d.put("contact_phone", "");
        DEFAULTS = Collections.unmodifiableMap(d);
    }

    private final SiteSettingRepository settings;
    private final AuditService audit;

    /** Receives database access for settings and the activity record. */
    public SettingsService(SiteSettingRepository settings, AuditService audit) {
        this.settings = settings;
        this.audit = audit;
    }

    /** Every setting with its current text: the saved value when there is one, the default otherwise. */
    @Transactional(readOnly = true)
    public Map<String, String> get() {
        Map<String, String> result = new LinkedHashMap<>(DEFAULTS);
        for (SiteSetting saved : settings.findAll()) {
            if (DEFAULTS.containsKey(saved.getKey())) {
                result.put(saved.getKey(), saved.getValue());
            }
        }
        return result;
    }

    /**
     * Saves the settings the admin sent and returns the full, updated list. Refuses unknown keys,
     * text longer than 500 characters and an empty shop name. Settings not sent are left as they are.
     */
    @Transactional
    public Map<String, String> update(Map<String, String> changes) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        for (Map.Entry<String, String> change : changes.entrySet()) {
            String key = change.getKey();
            String value = change.getValue() == null ? "" : change.getValue().trim();
            if (!DEFAULTS.containsKey(key)) {
                throw ApiException.badRequest("Unknown setting: " + key);
            }
            if (value.length() > MAX_LENGTH) {
                throw ApiException.badRequest("Text is too long (" + MAX_LENGTH + " characters at most)");
            }
            if (key.equals("shop_name") && value.isEmpty()) {
                throw ApiException.badRequest("The shop name cannot be empty");
            }
            SiteSetting setting = settings.findById(key).orElseGet(SiteSetting::new);
            setting.setKey(key);
            setting.setValue(value);
            setting.setUpdatedAt(now);
            settings.save(setting);
        }
        audit.record("SETTINGS_UPDATED", "Changed: " + String.join(", ", changes.keySet()));
        return get();
    }
}
