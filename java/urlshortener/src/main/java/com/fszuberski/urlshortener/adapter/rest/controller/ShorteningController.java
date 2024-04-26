package com.fszuberski.urlshortener.adapter.rest.controller;

import com.fszuberski.urlshortener.adapter.rest.model.ErrorResponseModel;
import com.fszuberski.urlshortener.adapter.rest.model.ShortenRequestModel;
import com.fszuberski.urlshortener.core.shortening.port.in.ShortenUrlUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URL;

@RestController
@RequestMapping("shorten")
@RequiredArgsConstructor
public class ShorteningController {

    private final ShortenUrlUseCase shortenUrlUseCase;

    @PostMapping
    public ResponseEntity<?> createShortUrl(@RequestBody ShortenRequestModel shortenRequestModel) {
        if (shortenRequestModel == null || shortenRequestModel.url() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponseModel("URL not present in request body"));
        }

        URL url;
        try {
            url = new URI(shortenRequestModel.url()).toURL();
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponseModel("Invalid URL"));
        }

        try {
            shortenUrlUseCase.shortenUrl(url);
        } catch (Exception e) {
            ResponseEntity.internalServerError();
        }

        return ResponseEntity.ok().body("ok");
    }
}
