package com.fszuberski.urlshortener.config;

import com.fszuberski.urlshortener.adapter.datasource.InMemoryDatasourceAdapter;
import com.fszuberski.urlshortener.adapter.rest.controller.ShorteningController;
import com.fszuberski.urlshortener.core.shortening.ShorteningConfiguration;
import com.fszuberski.urlshortener.core.shortening.port.in.ShortenUrlUseCase;
import com.fszuberski.urlshortener.core.shortening.port.out.SaveShortenedUrlPort;
import com.fszuberski.urlshortener.core.shortening.service.ShorteningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BeansConfiguration {

    @Bean
    SaveShortenedUrlPort saveShortenedUrlPort() {
        log.info("saveShortenedUrlPort being initialized");
        return new InMemoryDatasourceAdapter();
    }

    @Bean
    ShortenUrlUseCase shortenUrlUseCase(SaveShortenedUrlPort saveShortenedUrlPort, ApplicationProperties properties) {
        log.info("shortenUrlUseCase being initialized");
        return new ShorteningService(
                saveShortenedUrlPort,
                new ShorteningConfiguration(properties.baseUrl(), properties.keyLength(), properties.maxAttemptsOnCollisions()));
    }

    @Bean
    ShorteningController shorteningController(ShortenUrlUseCase shortenUrlUseCase) {
        log.info("shorteningController being initialized");
        return new ShorteningController(shortenUrlUseCase);
    }
}
