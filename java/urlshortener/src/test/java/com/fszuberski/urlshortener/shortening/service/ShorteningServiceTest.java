package com.fszuberski.urlshortener.shortening.service;

import com.fszuberski.urlshortener.core.shortening.ShorteningConfiguration;
import com.fszuberski.urlshortener.core.shortening.domain.ShorteningResult;
import com.fszuberski.urlshortener.core.shortening.exception.KeyCollisionException;
import com.fszuberski.urlshortener.core.shortening.port.out.SaveShortenedUrlPort;
import com.fszuberski.urlshortener.core.shortening.service.ShorteningService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ShorteningServiceTest {

    private ShorteningService shorteningService;
    private SaveShortenedUrlPort saveShortenedUrlPort;
    private final ShorteningConfiguration configuration =
            new ShorteningConfiguration("https://urlshortener.com/", 10, 16);

    @BeforeEach
    public void beforeEach() {
        saveShortenedUrlPort = mock(SaveShortenedUrlPort.class);
        shorteningService = new ShorteningService(saveShortenedUrlPort, configuration);
    }

    @Test
    public void shouldThrowExceptionGivenNullSaveShortenedUriPort() {
        assertThrows(IllegalArgumentException.class, () -> new ShorteningService(null, configuration));
    }

    @Test
    public void shouldThrowExceptionGivenNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new ShorteningService(saveShortenedUrlPort, null));
    }

    @Test
    public void shouldSaveShortenedUriWithoutRetriesGivenNoKeyCollision() throws URISyntaxException, MalformedURLException {
        var longUrl = new URI("https://example.com/some/long?uri#test").toURL();
        var key = RandomStringUtils.randomAlphanumeric(configuration.keyLength());
        when(saveShortenedUrlPort.saveShortenedUrl(eq(longUrl.toString()), any())).thenReturn(key);

        var result = shorteningService.shortenUrl(longUrl);
        var expectedResult = new ShorteningResult(longUrl, new URI("%s%s".formatted(configuration.baseUrl(), key)).toURL());

        assertEquals(expectedResult, result);
        verify(saveShortenedUrlPort, times(1)).saveShortenedUrl(any(), any());
    }

    @Test
    public void shouldSaveShortenedUriWithRetriesGivenKeyCollision() throws URISyntaxException, MalformedURLException {
        var longUrl = new URI("https://example.com/some/long?uri#test").toURL();
        var key = RandomStringUtils.randomAlphanumeric(configuration.keyLength());
        when(saveShortenedUrlPort.saveShortenedUrl(eq(longUrl.toString()), any()))
                .thenThrow(new KeyCollisionException(key))
                .thenReturn(key);

        var result = shorteningService.shortenUrl(longUrl);
        var expectedResult = new ShorteningResult(longUrl, new URI("%s%s".formatted(configuration.baseUrl(), key)).toURL());

        assertEquals(expectedResult, result);
        verify(saveShortenedUrlPort, times(2)).saveShortenedUrl(any(), any());
    }

    @Test
    public void shouldThrowExceptionGivenRetriesExhaustedOnKeyCollision() throws URISyntaxException, MalformedURLException {
        var longUrl = new URI("https://example.com/some/long?uri#test").toURL();
        var key = RandomStringUtils.randomAlphanumeric(configuration.keyLength());
        when(saveShortenedUrlPort.saveShortenedUrl(eq(longUrl.toString()), any())).thenThrow(new KeyCollisionException(key));

        assertThrows(KeyCollisionException.class, () -> shorteningService.shortenUrl(longUrl));
        verify(saveShortenedUrlPort, times(16)).saveShortenedUrl(any(), any());
    }

    @Test
    public void shouldThrowExceptionWithoutRetriesGivenExceptionOtherThanKeyCollision() throws URISyntaxException, MalformedURLException {
        var longUrl= new URI("https://example.com/some/long?uri#test").toURL();
        when(saveShortenedUrlPort.saveShortenedUrl(eq(longUrl.toString()), any())).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> shorteningService.shortenUrl(longUrl));
        verify(saveShortenedUrlPort, times(1)).saveShortenedUrl(any(), any());
    }

    @Test
    public void shouldThrowExceptionGivenPassedUriIsNull() {
        assertThrows(IllegalArgumentException.class, () -> shorteningService.shortenUrl(null));
    }
}