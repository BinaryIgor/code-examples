package com.binaryigor.vembeddingswithpostgres.shared;

import java.util.stream.Stream;

public record SizedStream<T>(Stream<T> stream, long size) {
}
