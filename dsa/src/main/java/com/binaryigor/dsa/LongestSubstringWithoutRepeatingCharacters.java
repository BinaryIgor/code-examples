package com.binaryigor.dsa;

import java.util.HashSet;
import java.util.List;

public class LongestSubstringWithoutRepeatingCharacters {

    public static int length(String s) {
        var longest = 0;
        var nonRepeating = new HashSet<Character>();

        int i = 0;
        int nextCandidateStart = 1;
        while (i < s.length()) {
            var c = s.charAt(i);
            var newElement = nonRepeating.add(c);
            if (newElement) {
                i++;
            } else {
                i = nextCandidateStart;
                nextCandidateStart++;

                if (nonRepeating.size() > longest) {
                    longest = nonRepeating.size();
                }

                nonRepeating.clear();
            }
        }

        if (nonRepeating.size() > longest) {
            longest = nonRepeating.size();
        }

        return longest;
    }

    record Case(String input, int expected) {

        static List<Case> cases() {
            return List.of(
                    new Case("abcabcbb", 3),
                    new Case("bbbbb", 1),
                    new Case("pwwkew", 3),
                    new Case("dvdf", 3),
                    new Case("tmmzuxt", 5),
                    new Case("bbtablud", 6)
            );
        }
    }
}
