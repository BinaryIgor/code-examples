package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class TopKFrequentWordsTest {

    @ParameterizedTest
    @MethodSource
    void returnsTopKFrequent(TopKFrequentWords.Case c) {
        var actual = TopKFrequentWords.topKFrequent(c.words(), c.k());
        Assertions.assertEquals(c.expected(), actual,
                "Expected to get %s top k frequent for %s input and %d k but got"
                        .formatted(c.expected(), Arrays.toString(c.words()), c.k(), actual));
    }

    static List<TopKFrequentWords.Case> returnsTopKFrequent() {
        return TopKFrequentWords.Case.cases();
    }
}
