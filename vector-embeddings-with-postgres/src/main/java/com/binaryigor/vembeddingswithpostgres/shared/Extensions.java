package com.binaryigor.vembeddingswithpostgres.shared;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.Callable;

public class Extensions {

    public static <T> TimedResult<T> timed(Callable<T> function) {
        try {
            var start = Instant.now();
            var result = function.call();
            var time = Duration.between(start, Instant.now());
            return new TimedResult<>(time, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String hashBasedId(MessageDigest hashDigest, int maxLength, String... components) {
        var hash = hashDigest.digest(String.join("", components).getBytes(StandardCharsets.UTF_8));
        var hashHex = HexFormat.of().formatHex(hash);
        return hashHex.length() > maxLength ? hashHex.substring(0, maxLength) : hashHex;
    }

    public record TimedResult<T>(Duration time, T result) {
    }
}
