package com.binaryigor.dsa;

import java.util.ArrayDeque;
import java.util.List;

/*
Prompt
You are designing the logic for a simple game. The game involves two players and an array of unique numbers.
The goal is to determine if the starting player can win assuming both players play optimally.

Requirements:
Game Rules:

Players (Player 1 and Player 2) take turns. Player 1 goes first.

There is a line of numbers (an integer array).

On each turn, a player picks one number from either end of the array (left or right).

The chosen number is removed, and its value is added to the player's score.

The game ends when the array is empty.

Objective:

Return true if Player 1 can win (score > Player 2's score).

Return false otherwise.

Crucial Constraint: Assume both players play optimally (they always make the move that maximizes their own winning chances).

Examples:

[1, 5, 2] -> False. (P1 picks 1 or 2 -> P2 picks 5 -> P1 picks remaining. Score: 3 vs 5).

[1, 5, 233, 7] -> True. (P1 picks 1 -> P2 picks 5 or 7 -> P1 picks 233 -> P2 picks remaining. Score: 234 vs 12).

Note: P2's choice between 5 and 7 doesn't affect the outcome - both lead to P2 scoring 12.

[1, 4, 5, 2] -> False (tie). P1 picks 1 -> P2 picks 4 or 2 -> both lead to 6 vs 6. With optimal play, P1 cannot do better than tie.
 */
public class ArrayGame {

    static boolean play(int[] numbers) {
        var allScore = 0;
        for (var n : numbers) {
            allScore += n;
        }
//        var memo = new boolean[numbers.length][numbers.length];
//        var player1Score = bestScore(numbers, 0, numbers.length - 1, 0, true, memo);
        var player1Score = bestScoreStack(numbers);
        var player2Score = allScore - player1Score;

        return player1Score > player2Score;
    }

    private static int bestScore(int[] numbers, int startIdx, int endIdx,
                                 int currentScore, boolean playerMove,
                                 boolean[][] memo) {
        if (startIdx >= endIdx || memo[startIdx][endIdx]) {
            return currentScore;
        }

        memo[startIdx][endIdx] = true;

        var leftNumber = numbers[startIdx];
        if ((endIdx - startIdx) == 1) {
            return currentScore + (playerMove ? leftNumber : 0);
        }
        var rightNumber = numbers[endIdx];

        var leftNumberScore = bestScore(numbers, startIdx + 1, endIdx,
                currentScore + (playerMove ? leftNumber : 0), !playerMove, memo);

        var rightNumberScore = bestScore(numbers, startIdx, endIdx - 1,
                currentScore + (playerMove ? rightNumber : 0), !playerMove, memo);

        return Math.max(leftNumberScore, rightNumberScore);
    }

    private static int bestScoreStack(int[] numbers) {
        var memo = new boolean[numbers.length][numbers.length];

        var nextRoundStack = new ArrayDeque<NextRoundState>();
        nextRoundStack.push(new NextRoundState(0, numbers.length - 1, 0, true));

        var bestScore = 0;

        while (!nextRoundStack.isEmpty()) {
            var roundState = nextRoundStack.pop();
            var startIdx = roundState.startIdx();
            var endIdx = roundState.endIdx();
            var currentScore = roundState.currentScore();
            var playerMove = roundState.playerMove();

            if (startIdx >= endIdx || memo[startIdx][endIdx]) {
                continue;
            }

            memo[startIdx][endIdx] = true;

            var leftNumber = numbers[startIdx];
            if ((endIdx - startIdx) == 1) {
                var score = currentScore + (playerMove ? leftNumber : 0);
                if (score > bestScore) {
                    bestScore = score;
                }
            } else {
                var rightNumber = numbers[endIdx];

                var leftNumberState = new NextRoundState(startIdx + 1, endIdx,
                        currentScore + (playerMove ? leftNumber : 0), !playerMove);
                var rightNumberState = new NextRoundState(startIdx, endIdx - 1,
                        currentScore + (playerMove ? rightNumber : 0), !playerMove);

                nextRoundStack.push(leftNumberState);
                nextRoundStack.push(rightNumberState);
            }
        }

        return bestScore;
    }

    private record NextRoundState(int startIdx, int endIdx, int currentScore, boolean playerMove) {
    }

    record Case(int[] numbers, boolean expected) {
        static List<Case> cases() {
            return List.of(
                    new Case(new int[]{1, 5, 2}, false),
                    new Case(new int[]{1, 5, 233, 7}, true),
                    new Case(new int[]{1, 4, 5, 2}, false)
            );
        }
    }
}
