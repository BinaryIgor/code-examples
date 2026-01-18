package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class BalancedBinaryTreeTest {

    @ParameterizedTest
    @MethodSource
    void returnsWhetherTreeIsBalanced(BalancedBinaryTree.Case c) {
        var actual = BalancedBinaryTree.isBalanced(c.tree());
        Assertions.assertEquals(c.balanced(), actual,
                "%s tree is expected to be balanced=%b, but was=%b"
                        .formatted(c.tree(), c.balanced(), actual));
    }

    static List<BalancedBinaryTree.Case> returnsWhetherTreeIsBalanced() {
        return BalancedBinaryTree.Case.cases();
    }
}