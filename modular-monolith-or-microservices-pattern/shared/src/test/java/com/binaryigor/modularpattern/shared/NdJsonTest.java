package com.binaryigor.modularpattern.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

public class NdJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void writesObjectsInTheNewlineDelimitedJsonFormatAndReadsFromIt() {
        var toWriteObjects = List.of(SomeObject.random(), SomeObject.random(), SomeObject.random());

        var output = new ByteArrayOutputStream();

        NdJson.writeTo(toWriteObjects.stream(), objectMapper, output);

        var ndJson = output.toString();
        var writtenObjects = NdJson.readFrom(ndJson, objectMapper, SomeObject.class);

        Assertions.assertThat(writtenObjects)
            .containsExactlyElementsOf(toWriteObjects);
    }

    record SomeObject(UUID id, String name) {

        static SomeObject random() {
            return new SomeObject(UUID.randomUUID(), UUID.randomUUID() + "-name");
        }
    }
}
