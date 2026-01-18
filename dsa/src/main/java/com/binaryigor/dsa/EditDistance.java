package com.binaryigor.dsa;

import java.util.List;

public class EditDistance {

    static int minDistance(String word1, String word2) {
        var memo = new Integer[word1.length()][word2.length()];
        return findMinDistance(word1, word2, 0, 0, memo);
    }

    private static int findMinDistance(String word1, String word2,
                                       int word1Idx, int word2Idx,
                                       Integer[][] memo) {
        if (word1Idx >= word1.length()) {
            return word2.length() - word2Idx;
        }
        if (word2Idx >= word2.length()) {
            return word1.length() - word1Idx;
        }

        if (memo[word1Idx][word2Idx] != null) {
            return memo[word1Idx][word2Idx];
        }

        if (word1.charAt(word1Idx) == word2.charAt(word2Idx)) {
            memo[word1Idx][word2Idx] = findMinDistance(word1, word2, word1Idx + 1, word2Idx + 1, memo);
        } else {
            var insertDist = findMinDistance(word1, word2, word1Idx, word2Idx + 1, memo);
            var deleteDist = findMinDistance(word1, word2, word1Idx + 1, word2Idx, memo);
            var replaceDist = findMinDistance(word1, word2, word1Idx + 1, word2Idx + 1, memo);
            memo[word1Idx][word2Idx] = 1 + Math.min(Math.min(insertDist, deleteDist), replaceDist);
        }

        return memo[word1Idx][word2Idx];
    }

    record Case(String word1, String word2, int expected) {
        static List<Case> cases() {
            return List.of(
                    new Case("horse", "ros", 3),
                    new Case("intention", "execution", 5),
                    new Case("ala", "ala", 0),
                    new Case("change everything", "0", 17),
                    new Case("almos", "almost", 1)
            );
        }
    }
}
