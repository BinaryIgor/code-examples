package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class TopKFrequentElementsTest {

    @ParameterizedTest
    @MethodSource
    void returnsTopKFrequentElements(TopKFrequentElements.Case c) {
        var actual = TopKFrequentElements.topKFrequent(c.nums(), c.k());
        Assertions.assertArrayEquals(c.expected(), actual,
                "Expected %s as most frequent, but got %s for nums=%s, k=%d"
                        .formatted(Arrays.toString(c.expected()), Arrays.toString(actual),
                                Arrays.toString(c.nums()), c.k()));
    }

    static List<TopKFrequentElements.Case> returnsTopKFrequentElements() {
        return TopKFrequentElements.Case.cases();
    }
}
