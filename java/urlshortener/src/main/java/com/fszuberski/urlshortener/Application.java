package com.fszuberski.urlshortener;

import com.fszuberski.urlshortener.config.ApplicationProperties;
import com.fszuberski.urlshortener.config.BeansConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({BeansConfiguration.class})
@EnableConfigurationProperties(ApplicationProperties.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
