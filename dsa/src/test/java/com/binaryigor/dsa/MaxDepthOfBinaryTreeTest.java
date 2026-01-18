package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class MaxDepthOfBinaryTreeTest {

    @ParameterizedTest
    @MethodSource
    void returnsTreeMaxDepth(MaxDepthOfBinaryTree.Case c) {
        var actual = MaxDepthOfBinaryTree.maxDepth(c.tree());
        Assertions.assertEquals(c.maxDepth(), actual,
                "Expected %d maxDepth of %s tree, but got %d"
                        .formatted(c.maxDepth(), c.tree(), actual));
    }

    static List<MaxDepthOfBinaryTree.Case> returnsTreeMaxDepth() {
        return MaxDepthOfBinaryTree.Case.cases();
    }
}
