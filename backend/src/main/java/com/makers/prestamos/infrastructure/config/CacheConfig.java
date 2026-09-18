package com.makers.prestamos.infrastructure.config;

import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ehcache 3 vía JCache. Las cachés se declaran en {@code ehcache.xml}
 * y se enlazan con {@code spring.cache.jcache.config}.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String LOANS_BY_USER = "loansByUser";
    public static final String LOANS_BY_ID = "loansById";

    /**
     * Con {@code transactionAware} las invalidaciones se aplican al confirmar la transacción,
     * evitando que otra lectura concurrente vuelva a cachear el estado antiguo antes del commit.
     */
    @Bean
    CacheManagerCustomizer<JCacheCacheManager> transactionAwareCaches() {
        return manager -> manager.setTransactionAware(true);
    }
}
