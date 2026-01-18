package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class KthLargestElementTest {

    @ParameterizedTest
    @MethodSource
    void findsKthLargestElement(KthLargestElement.Case c) {
        var actual = KthLargestElement.find(c.nums(), c.k());
        Assertions.assertEquals(c.expected(), actual, "Expected to find %d element for %d k but got %d"
                .formatted(c.expected(), c.k(), actual));
    }

    static List<KthLargestElement.Case> findsKthLargestElement() {
        return KthLargestElement.Case.cases();
    }
}
