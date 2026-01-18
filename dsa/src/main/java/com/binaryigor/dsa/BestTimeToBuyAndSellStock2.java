package com.binaryigor.dsa;

import java.util.List;

public class BestTimeToBuyAndSellStock2 {

    static int maxProfit(int[] prices) {
        if (prices.length <= 1) {
            return 0;
        }
        int totalProfit = 0;
        for (int i = 1; i < prices.length; i++) {
            totalProfit += Math.max(0, prices[i] - prices[i - 1]);
        }
        return totalProfit;
    }

    record Case(int[] prices, int expected) {

        static List<Case> cases() {
            return List.of(
                    new Case(new int[]{7, 1, 5, 3, 6, 4}, 7),
                    new Case(new int[]{1, 2, 4, 5}, 4),
                    new Case(new int[]{7, 6, 4, 3, 1}, 0)
            );
        }
    }
}
