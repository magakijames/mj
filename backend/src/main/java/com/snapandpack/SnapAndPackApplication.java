package com.snapandpack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The starting point of the backend. */
@SpringBootApplication
public class SnapAndPackApplication {

    /** Starts the web server, connects to the database and loads every other class in this project. */
    public static void main(String[] args) {
        SpringApplication.run(SnapAndPackApplication.class, args);
    }
}
