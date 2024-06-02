package com.binaryigor.modularpattern.shared;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StreamsTest {

    @Test
    void splitsStreamIntoChunks() {
        var sourceStream = Stream.of(1, 2, 3, 4, 5);
        var expectedChunks = List.of(
            List.of(1, 2),
            List.of(3, 4),
            List.of(5)
        );

        var consumedChunks = new ArrayList<List<Integer>>();

        Streams.chunked(sourceStream, 2)
            .forEach(consumedChunks::add);

        Assertions.assertThat(consumedChunks)
            .containsExactlyElementsOf(expectedChunks);
    }
}
