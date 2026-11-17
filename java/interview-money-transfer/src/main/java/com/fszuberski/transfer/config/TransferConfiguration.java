package com.fszuberski.transfer.config;

import com.fszuberski.transfer.core.NaiveTransferService;
import com.fszuberski.transfer.core.TransferStore;
import com.fszuberski.transfer.core.TransferUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransferConfiguration {
    @Bean
    TransferUseCase transferUseCase(TransferStore store) {
        return new NaiveTransferService(store);
    }
}
