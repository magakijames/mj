package com.snapandpack.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Works out the public web address of the site, used when building links such as password resets. */
@Component
public class SiteUrl {

    private final String configured;

    /** Receives app.public-url from application.properties; empty means "use the address of the request". */
    public SiteUrl(@Value("${app.public-url:}") String configured) {
        this.configured = configured == null ? "" : configured.trim().replaceAll("/+$", "");
    }

    /** The site address without a trailing slash, for example http://localhost:8080. */
    public String current() {
        return configured.isEmpty() ? ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() : configured;
    }
}
