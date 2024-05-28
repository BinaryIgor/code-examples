package com.binaryigor.modularpattern.shared;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

// Newline delimited JSON, where each object is written on a new line
public class NdJson {

    public static void writeTo(Stream<?> objectsStream, ObjectMapper objectMapper, OutputStream out) {
        try (var objects = objectsStream) {
            objects.forEach(o -> writeObjectLine(out, objectMapper, o));
        }
    }

    private static void writeObjectLine(OutputStream out, ObjectMapper objectMapper, Object object) {
        try {
            var json = objectMapper.writeValueAsString(object);
            out.write((json + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Problem while writing an object to the OutputStream", e);
        }
    }

    public static <T> Stream<T> readFrom(Stream<String> lines, ObjectMapper objectMapper, Class<T> type) {
        return lines.map(l -> readObjectFromLine(l, objectMapper, type));
    }

    public static <T> Stream<T> readFrom(String lines, ObjectMapper objectMapper, Class<T> type) {
        if (lines == null || lines.isBlank()) {
            return Stream.empty();
        }
        return readFrom(Stream.of(lines.split("\n")), objectMapper, type);
    }

    private static <T> T readObjectFromLine(String line, ObjectMapper objectMapper, Class<T> type) {
        try {
            return objectMapper.readValue(line, type);
        } catch (Exception e) {
            throw new RuntimeException("Problem while reading object line of %s type".formatted(type), e);
        }
    }
}
