package com.binaryigor.dsa;

import java.util.List;

public class BestTimeToBuyAndSellStock {

    static int maxProfit(int[] prices) {
        if (prices.length <= 1) {
            return 0;
        }
        int cheapest = prices[0];
        int bestProfit = 0;

        for (int i = 1; i < prices.length; i++) {
            int price = prices[i];
            int profit = price - cheapest;
            if (profit > bestProfit) {
                bestProfit = profit;
            }
            if (cheapest > price) {
                cheapest = price;
            }
        }

        return bestProfit;
    }


    record Case(int[] prices, int expected) {

        static List<Case> cases() {
            return List.of(
                    new Case(new int[]{7, 1, 5, 3, 6, 4}, 5),
                    new Case(new int[]{7, 6, 4, 3, 1}, 0),
                    new Case(new int[]{7, 2, 5, 3, 6, 4, 7, 11}, 9),
                    new Case(new int[]{11, 11, 1, 3, 1}, 2)
            );
        }
    }
}
