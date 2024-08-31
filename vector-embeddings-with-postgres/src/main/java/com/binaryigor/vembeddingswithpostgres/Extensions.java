package com.binaryigor.vembeddingswithpostgres;

import java.time.Duration;
import java.time.Instant;
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

    public record TimedResult<T>(Duration time, T result) {
    }
}
