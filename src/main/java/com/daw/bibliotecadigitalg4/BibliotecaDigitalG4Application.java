package com.daw.bibliotecadigitalg4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BibliotecaDigitalG4Application {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaDigitalG4Application.class, args);
    }

}
