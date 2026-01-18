package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class LongestSubstringWithoutRepeatingCharactersTest {

    @ParameterizedTest
    @MethodSource
    void returnsLengthOfLongestSubstringWithoutRepeatingCharacters(LongestSubstringWithoutRepeatingCharacters.Case c) {
        var actual = LongestSubstringWithoutRepeatingCharacters.length(c.input());
        Assertions.assertEquals(c.expected(), actual, "Expected length %d, but got %d for %s input"
                .formatted(c.expected(), actual, c.input()));
    }

    static List<LongestSubstringWithoutRepeatingCharacters.Case> returnsLengthOfLongestSubstringWithoutRepeatingCharacters() {
        return LongestSubstringWithoutRepeatingCharacters.Case.cases();
    }
}
