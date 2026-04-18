package com.binaryigor.complexity_alternative.domain;

import java.math.BigDecimal;

public record Money(BigDecimal value, Currency currency) {

    public static Money euro(String value) {
        return new Money(new BigDecimal(value), Currency.EUR);
    }

    public enum Currency {
        EUR
    }
}
