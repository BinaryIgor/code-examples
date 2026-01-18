package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class BinaryTreeLevelOrderTraversalTest {

    @ParameterizedTest
    @MethodSource
    void levelOrderReturnsByLevelOrder(BinaryTreeLevelOrderTraversal.Case c) {
        var actual = BinaryTreeLevelOrderTraversal.levelOrder(c.tree());
        Assertions.assertEquals(c.expectedLevelOrderTraversal(), actual,
                "Unexpected traversal! Expected %s, but got %s"
                        .formatted(c.expectedLevelOrderTraversal(), actual));
    }

    static List<BinaryTreeLevelOrderTraversal.Case> levelOrderReturnsByLevelOrder() {
        return BinaryTreeLevelOrderTraversal.Case.cases();
    }
}
