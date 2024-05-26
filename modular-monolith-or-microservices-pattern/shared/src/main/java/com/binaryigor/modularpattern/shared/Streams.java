package com.binaryigor.modularpattern.shared;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Streams {

    public static <T> Stream<List<T>> chunked(Stream<T> stream, int chunkSize) {
        var iterator = stream.iterator();
        var chunksIterator = new Iterator<List<T>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public List<T> next() {
                var nextChunk = new ArrayList<T>(chunkSize);
                while (hasNext() && nextChunk.size() < chunkSize) {
                    nextChunk.add(iterator.next());
                }
                return nextChunk;
            }
        };

        return StreamSupport.stream(((Iterable<List<T>>) () -> chunksIterator).spliterator(), false);
    }
}
