package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class ArrayGameTest {

    @ParameterizedTest
    @MethodSource
    void playsGameAccordingToRules(ArrayGame.Case c) {
        var actual = ArrayGame.play(c.numbers());
        Assertions.assertEquals(c.expected(), actual,
                "Expected player1 to win=%b in game, but got=%b for %s array"
                        .formatted(c.expected(), actual, Arrays.toString(c.numbers())));
    }

    static List<ArrayGame.Case> playsGameAccordingToRules() {
        return ArrayGame.Case.cases();
    }
}
