package com.binaryigor.dsa;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BalancedBinaryTree {

    static boolean isBalanced(TreeNode root) {
        if (root == null) {
            return true;
        }
        var balanced = new AtomicBoolean(true);

        var leftHeight = calculateNodeHeight(root.left, 0, balanced);
        if (!balanced.get()) {
            return false;
        }
        var rightHeight = calculateNodeHeight(root.right, 0, balanced);
        if (!balanced.get()) {
            return false;
        }

        return Math.abs(leftHeight - rightHeight) <= 1;
    }

    private static int calculateNodeHeight(TreeNode node, int currentHeight, AtomicBoolean balanced) {
        if (!balanced.get()) {
            return currentHeight;
        }
        if (node == null) {
            return -1;
        }

        var leftHeight = node.left == null ? currentHeight : calculateNodeHeight(node.left, currentHeight + 1, balanced);
        var rightHeight = node.right == null ? currentHeight : calculateNodeHeight(node.right, currentHeight + 1, balanced);

        if (Math.abs(leftHeight - rightHeight) > 1) {
            balanced.set(false);
        }

        return Math.max(leftHeight, rightHeight);
    }

    record Case(TreeNode tree, boolean balanced) {

        static List<Case> cases() {
            return List.of(
                    new Case(new TreeNode(3,
                            new TreeNode(9),
                            new TreeNode(20,
                                    new TreeNode(15),
                                    new TreeNode(7))),
                            true),
                    new Case(new TreeNode(1,
                            new TreeNode(2,
                                    new TreeNode(3, new TreeNode(3), new TreeNode(4)),
                                    new TreeNode(3)),
                            new TreeNode(2)),
                            false),
                    new Case(null, true),
                    new Case(new TreeNode(1, null, new TreeNode(2, null, new TreeNode(3))), false),
                    new Case(new TreeNode(1,
                            new TreeNode(2, new TreeNode(3, new TreeNode(4), null), null),
                            new TreeNode(2, new TreeNode(3, new TreeNode(4), null), null)),
                            false)
            );
        }
    }
}
