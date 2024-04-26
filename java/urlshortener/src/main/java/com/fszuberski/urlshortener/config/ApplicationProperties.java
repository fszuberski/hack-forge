package com.fszuberski.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Immutable properties in Spring:
// https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config.typesafe-configuration-properties.constructor-binding

@ConfigurationProperties(prefix = "urlshortener")
public record ApplicationProperties(String baseUrl, int keyLength, int maxAttemptsOnCollisions) {
}
