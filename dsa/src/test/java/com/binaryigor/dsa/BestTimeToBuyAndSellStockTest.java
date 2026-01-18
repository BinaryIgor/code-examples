package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class BestTimeToBuyAndSellStockTest {

    @ParameterizedTest
    @MethodSource
    void returnsMaxProfit(BestTimeToBuyAndSellStock.Case c) {
        var actual = BestTimeToBuyAndSellStock.maxProfit(c.prices());
        Assertions.assertEquals(c.expected(), actual,
                "Got %d profit, but expected %d for %s prices"
                        .formatted(actual, c.expected(), Arrays.toString(c.prices())));
    }

    static List<BestTimeToBuyAndSellStock.Case> returnsMaxProfit() {
        return BestTimeToBuyAndSellStock.Case.cases();
    }
}
