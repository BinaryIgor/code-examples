package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class SubarraySumTest {

    @ParameterizedTest
    @MethodSource
    void returnsMatchingBySumSubarraysNumber(SubarraySum.Case c) {
        var actual = SubarraySum.sum(c.nums(), c.k());
        Assertions.assertEquals(c.expectedK(), actual,
                "Expected to get %d matching by sum subarrays, but got %d for %s subarray"
                        .formatted(c.expectedK(), c.k(), Arrays.toString(c.nums())));
    }

    static List<SubarraySum.Case> returnsMatchingBySumSubarraysNumber() {
        return SubarraySum.Case.cases();
    }
}
