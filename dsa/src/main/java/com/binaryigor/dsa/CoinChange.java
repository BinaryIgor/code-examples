package com.binaryigor.dsa;

import java.util.Arrays;
import java.util.List;

public class CoinChange {

    static int coinChange(int[] coins, int amount) {
        // dp must have all possible amounts + 0 - base case
        var dp = new int[amount + 1];
        // just a value signaling that solution was not found yet - greater than target amount
        Arrays.fill(dp, amount + 1);
        dp[0] = 0; // base case - 0 coins to make 0 amount

        // iterate over all amounts and create state of how many coins must be used to make given amount
        for (var subAmount = 1; subAmount <= amount; subAmount++) {
            for (var coin : coins) {
                var subAmountRest = subAmount - coin;
                if (subAmountRest >= 0) {
                    // take minimum of either current solution, initialized as amount + 1,
                    // or the rest amount solution + 1 (current coin)
                    dp[subAmount] = Math.min(dp[subAmount], dp[subAmountRest] + 1);
                }
            }
        }

        // if greater than amount, it is the initialized at start value so no solution
        return dp[amount] > amount ? -1 : dp[amount];
    }

    record Case(int[] coins, int amount, int expected) {

        static List<Case> cases() {
            return List.of(
                    new Case(new int[]{411, 412, 413, 414, 415, 416, 417, 418, 419, 420, 421, 422}, 9864, 24),
                    new Case(new int[]{1, 2, 5}, 11, 3),
                    new Case(new int[]{1, 2, 5, 8}, 63, 9),
                    new Case(new int[]{2}, 3, -1),
                    new Case(new int[]{186, 419, 83, 408}, 6249, 20),
                    new Case(new int[]{1}, 0, 0),
                    new Case(new int[]{1, 2, 4, 5}, 8, 2),
                    new Case(new int[]{1, 2, 4, 5}, 11, 3),
                    new Case(new int[]{1, 2}, 200, 100)
            );
        }
    }
}
