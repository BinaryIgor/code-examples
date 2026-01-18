package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class DijkstraTest {

    @ParameterizedTest
    @MethodSource
    void returnsShortestDistances(Dijkstra.Case c) {
        var actual = Dijkstra.shortestDistances(c.graph(), c.source());
        Assertions.assertEquals(c.expected(), actual,
                "Expected %s shortest distances but got %s for %s graph and %d source"
                        .formatted(c.expected(), actual, c.graph(), c.source()));

    }

    static List<Dijkstra.Case> returnsShortestDistances() {
        return Dijkstra.Case.cases();
    }
}
