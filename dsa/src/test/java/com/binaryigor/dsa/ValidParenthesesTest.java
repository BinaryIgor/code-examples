package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class ValidParenthesesTest {

    @ParameterizedTest
    @MethodSource
    void returnsWhetherParenthesesAreValid(ValidParentheses.Case c) {
        var actual = ValidParentheses.isValid(c.string());
        Assertions.assertEquals(c.isValid(), actual,
                "Expected %s isValid but got %s for %s string"
                        .formatted(c.isValid(), actual, c.string()));
    }

    static List<ValidParentheses.Case> returnsWhetherParenthesesAreValid() {
        return ValidParentheses.Case.cases();
    }
}
