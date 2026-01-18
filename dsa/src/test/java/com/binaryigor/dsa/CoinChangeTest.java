package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class CoinChangeTest {

    @ParameterizedTest
    @MethodSource
    void returnsCoinChange(CoinChange.Case c) {
        var actual = CoinChange.coinChange(c.coins(), c.amount());
        Assertions.assertEquals(c.expected(), actual,
                "Got %d coin change for %s coins and %d amount but %d was expected"
                        .formatted(actual, Arrays.toString(c.coins()), c.amount(), c.expected()));
    }

    static List<CoinChange.Case> returnsCoinChange() {
        return CoinChange.Case.cases();
    }
}
