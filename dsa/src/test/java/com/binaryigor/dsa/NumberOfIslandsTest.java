package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class NumberOfIslandsTest {

    @ParameterizedTest
    @MethodSource
    void returnsNumberOfIslands(NumberOfIslands.Case c) {
        var actual = NumberOfIslands.number(c.grid());
        Assertions.assertEquals(c.islands(), actual, "Expected %d islands but got %d for %s grid"
                .formatted(c.islands(), actual, Arrays.deepToString(c.grid())));
    }


    static List<NumberOfIslands.Case> returnsNumberOfIslands() {
        return NumberOfIslands.Case.cases();
    }
}
