package com.fszuberski.urlshortner.shortening.service;

import com.fszuberski.urlshortner.shortening.ShorteningConfiguration;
import com.fszuberski.urlshortner.shortening.domain.ShorteningResult;
import com.fszuberski.urlshortner.shortening.exception.KeyCollisionException;
import com.fszuberski.urlshortner.shortening.port.out.SaveShortenedUriPort;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ShorteningServiceTest {

    private ShorteningService shorteningService;
    private SaveShortenedUriPort saveShortenedUriPort;
    private final ShorteningConfiguration configuration =
            new ShorteningConfiguration("https://urlshortner.com/", 10, 16);

    @BeforeEach
    public void beforeEach() {
        saveShortenedUriPort = mock(SaveShortenedUriPort.class);
        shorteningService = new ShorteningService(saveShortenedUriPort, configuration);
    }

    @Test
    public void shouldThrowExceptionGivenNullSaveShortenedUriPort() {
        assertThrows(IllegalArgumentException.class, () -> new ShorteningService(null, configuration));
    }

    @Test
    public void shouldThrowExceptionGivenNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new ShorteningService(saveShortenedUriPort, null));
    }

    @Test
    public void shouldSaveShortenedUriWithoutRetriesGivenNoKeyCollision() throws URISyntaxException {
        var longUri = new URI("https://example.com/some/long?uri#test");
        var key = RandomStringUtils.randomAlphanumeric(configuration.keyLength());
        when(saveShortenedUriPort.saveShortenedUri(eq(longUri), any())).thenReturn(key);

        var result = shorteningService.shortenUri(longUri);
        var expectedResult = new ShorteningResult(longUri, new URI("%s%s".formatted(configuration.baseUrl(), key)));

        assertEquals(expectedResult, result);
        verify(saveShortenedUriPort, times(1)).saveShortenedUri(any(), any());
    }

    @Test
    public void shouldSaveShortenedUriWithRetriesGivenKeyCollision() throws URISyntaxException {
        var longUri = new URI("https://example.com/some/long?uri#test");
        var key = RandomStringUtils.randomAlphanumeric(configuration.keyLength());
        when(saveShortenedUriPort.saveShortenedUri(eq(longUri), any()))
                .thenThrow(new KeyCollisionException(key))
                .thenReturn(key);


        var result = shorteningService.shortenUri(longUri);
        var expectedResult = new ShorteningResult(longUri, new URI("%s%s".formatted(configuration.baseUrl(), key)));

        assertEquals(expectedResult, result);
        verify(saveShortenedUriPort, times(2)).saveShortenedUri(any(), any());
    }

    @Test
    public void shouldThrowExceptionGivenRetriesExhaustedOnKeyCollision() throws URISyntaxException {
        var longUri = new URI("https://example.com/some/long?uri#test");
        var key = RandomStringUtils.randomAlphanumeric(configuration.keyLength());
        when(saveShortenedUriPort.saveShortenedUri(eq(longUri), any())).thenThrow(new KeyCollisionException(key));


        assertThrows(KeyCollisionException.class, () -> shorteningService.shortenUri(longUri));
        verify(saveShortenedUriPort, times(16)).saveShortenedUri(any(), any());
    }

    @Test
    public void shouldThrowExceptionWithoutRetriesGivenExceptionOtherThanKeyCollision() throws URISyntaxException {
        var longUri = new URI("https://example.com/some/long?uri#test");
        var key = RandomStringUtils.randomAlphanumeric(configuration.keyLength());
        when(saveShortenedUriPort.saveShortenedUri(eq(longUri), any())).thenThrow(new RuntimeException());


        assertThrows(RuntimeException.class, () -> shorteningService.shortenUri(longUri));
        verify(saveShortenedUriPort, times(1)).saveShortenedUri(any(), any());
    }

    @Test
    public void shouldThrowExceptionGivenPassedUriIsNull() {
        assertThrows(IllegalArgumentException.class, () -> shorteningService.shortenUri(null));
    }
}