package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class ContainerWithMostWaterTest {

    @ParameterizedTest
    @MethodSource
    void returnsMaxArea(ContainerWithMostWater.Case c) {
        var actual = ContainerWithMostWater.maxArea(c.height());
        Assertions.assertEquals(c.area(), actual, "Expected %d area for %s heights but got %d"
                .formatted(c.area(), Arrays.toString(c.height()), actual));
    }

    static List<ContainerWithMostWater.Case> returnsMaxArea() {
        return ContainerWithMostWater.Case.cases();
    }
}
