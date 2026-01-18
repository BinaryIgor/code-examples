package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class EditDistanceTest {

    @ParameterizedTest
    @MethodSource
    void returnsEditDistance(EditDistance.Case c) {
        var actual = EditDistance.minDistance(c.word1(), c.word2());
        Assertions.assertEquals(c.expected(), actual,
                "Got %d edit distance for %s and %s words but %d was expected"
                        .formatted(actual, c.word1(), c.word2(), c.expected()));
    }

    static List<EditDistance.Case> returnsEditDistance() {
        return EditDistance.Case.cases();
    }
}
