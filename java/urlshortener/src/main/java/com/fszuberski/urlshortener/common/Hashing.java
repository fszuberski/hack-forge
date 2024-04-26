package com.fszuberski.urlshortener.common;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Hashing {

    public static String generateShortHash(String data, Integer additionalData, int length) {
        return generateShortHash(data, ByteBuffer.allocate(4).putInt(additionalData).array(), length);
    }

    public static String generateShortHash(String data, byte[] additionalData, int length) {
        var bArray = ArrayUtils.addAll(data.getBytes(StandardCharsets.UTF_8), additionalData);
        var hash = DigestUtils.sha1Hex(bArray);
        return hash.substring(0, Math.min(length, hash.length()));
    }
}
