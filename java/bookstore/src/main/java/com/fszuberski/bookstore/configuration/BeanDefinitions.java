package com.fszuberski.bookstore.configuration;

import com.fszuberski.bookstore.availability.adapter.persistence.BookAvailabilityInMemoryAdapter;
import com.fszuberski.bookstore.availability.core.service.BookAvailabilityService;
import com.fszuberski.bookstore.availability.port.in.CheckBookAvailabilityUseCase;
import com.fszuberski.bookstore.availability.port.out.GetBookAvailabilityPort;
import org.springframework.context.annotation.Bean;

public class BeanDefinitions {

    @Bean
    public CheckBookAvailabilityUseCase checkBookAvailabilityUseCase(GetBookAvailabilityPort getBookAvailabilityPort) {
        return new BookAvailabilityService(getBookAvailabilityPort);
    }

    @Bean
    public GetBookAvailabilityPort getBookAvailabilityPort() {
        return new BookAvailabilityInMemoryAdapter(true, true); // todo: externalise configuration
    }

}
