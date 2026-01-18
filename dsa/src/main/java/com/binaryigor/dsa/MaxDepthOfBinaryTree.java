package com.binaryigor.dsa;

import java.util.List;

public class MaxDepthOfBinaryTree {

    static int maxDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        return depth(root, 1);
    }

    private static int depth(TreeNode node, int currentDepth) {
        var leftDepth = node.left == null ? currentDepth : depth(node.left, currentDepth + 1);
        var rightDepth = node.right == null ? currentDepth : depth(node.right, currentDepth + 1);
        return Math.max(leftDepth, rightDepth);
    }

    record Case(TreeNode tree, int maxDepth) {

        static List<Case> cases() {
            return List.of(
                    new Case(new TreeNode(3,
                            new TreeNode(9),
                            new TreeNode(20, new TreeNode(15), new TreeNode(7))),
                            3),
                    new Case(new TreeNode(1, null, new TreeNode(2)), 2));
        }
    }
}
