package com.binaryigor.vembeddingswithpostgres.shared;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class CsvFileTest {

    @ParameterizedTest
    @MethodSource("columnsTestCases")
    void returnsRowColumns(ColumnsTestCase testCase) {
        Assertions.assertThat(CsvFile.columns(testCase.row, testCase.separator))
            .isEqualTo(testCase.columns);
    }

    static List<ColumnsTestCase> columnsTestCases() {
        return List.of(
            new ColumnsTestCase("a", ",", List.of("a")),
            new ColumnsTestCase("\"a\",b,\"c, d, e\",f,\"h\"",
                ",",
                List.of("a", "b", "c, d, e", "f", "h")),
            new ColumnsTestCase("a, b;c", ";", List.of("a, b", "c"))
        );
    }

    record ColumnsTestCase(String row, String separator, List<String> columns) {
    }
}
