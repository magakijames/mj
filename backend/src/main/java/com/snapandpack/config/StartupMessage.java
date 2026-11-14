package com.snapandpack.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Prints the address of the store once the server is ready. */
@Component
public class StartupMessage {

    private static final Logger log = LoggerFactory.getLogger(StartupMessage.class);

    @Value("${server.port}")
    private int port;

    @EventListener(ApplicationReadyEvent.class)
    public void printAddress() {
        log.info("");
        log.info("Snap & Pack is running. Open http://localhost:{} in your browser.", port);
        log.info("Admin area: http://localhost:{}/admin/index.html", port);
        log.info("");
    }
}
