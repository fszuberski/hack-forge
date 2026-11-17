package com.fszuberski.transfer.adapter.http;

import com.fszuberski.transfer.core.MissingAccount;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TransferErrors {
    @ExceptionHandler(MissingAccount.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> missingAccount() {
        return Map.of("error", "account_not_found");
    }
}
