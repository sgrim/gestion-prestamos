package com.makers.prestamos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PrestamosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrestamosApplication.class, args);
    }
}
