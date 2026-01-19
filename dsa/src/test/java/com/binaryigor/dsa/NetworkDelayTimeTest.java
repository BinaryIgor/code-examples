package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class NetworkDelayTimeTest {

    @ParameterizedTest
    @MethodSource
    void returnsBestTime(NetworkDelayTime.Case c) {
        var actual = NetworkDelayTime.bestTime(c.times(), c.n(), c.k());
        Assertions.assertEquals(c.expected(), actual,
                "Expected to get %d best time with %s graph but got %d"
                        .formatted(c.expected(), Arrays.deepToString(c.times()), actual));
    }

    static List<NetworkDelayTime.Case> returnsBestTime() {
        return NetworkDelayTime.Case.cases();
    }
}
