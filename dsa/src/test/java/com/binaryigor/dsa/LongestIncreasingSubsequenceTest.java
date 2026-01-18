package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class LongestIncreasingSubsequenceTest {

    @ParameterizedTest
    @MethodSource
    void returnsLength(LongestIncreasingSubsequence.Case c) {
        var actual = LongestIncreasingSubsequence.length(c.nums());
        Assertions.assertEquals(c.output(), actual,
                "Expected to get %d for %s input but got %d"
                        .formatted(c.output(), Arrays.toString(c.nums()), actual));
    }

    static List<LongestIncreasingSubsequence.Case> returnsLength() {
        return LongestIncreasingSubsequence.Case.cases();
    }
}
