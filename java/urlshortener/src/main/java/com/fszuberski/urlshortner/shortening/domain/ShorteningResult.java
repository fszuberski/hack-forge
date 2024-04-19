package com.fszuberski.urlshortner.shortening.domain;

import java.net.URI;

public record ShorteningResult(URI longUri, URI shortUri) {
}
