package com.makers.prestamos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /** Reloj inyectable: permite fijar el tiempo en los tests. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
