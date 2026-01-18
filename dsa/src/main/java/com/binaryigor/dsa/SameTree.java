package com.binaryigor.dsa;

import java.util.List;

public class SameTree {

    static void runCases() {
        Case.cases().forEach(c -> {
            var actual = isSameTree(c.p, c.q);
            if (actual != c.expected) {
                throw new RuntimeException("Trees of %s and %s roots should be equal=%b, but were %b"
                        .formatted(c.p.val, c.q.val, c.expected, actual));
            }
        });
    }

    static boolean isSameTree(TreeNode p, TreeNode q) {
        if (p == null && q == null) {
            return true;
        }
        if (p == null) {
            return false;
        }
        if (q == null) {
            return false;
        }
        if (p.val != q.val) {
            return false;
        }
        if (!isSameTree(p.left, q.left)) {
            return false;
        }
        return isSameTree(p.right, q.right);
    }

    record Case(TreeNode p, TreeNode q, boolean expected) {

        static List<Case> cases() {
            return List.of(
                    new Case(
                            new TreeNode(1, new TreeNode(2), new TreeNode(3)),
                            new TreeNode(1, new TreeNode(2), new TreeNode(3)),
                            true
                    ),
                    new Case(
                            new TreeNode(1, new TreeNode(2), new TreeNode(1)),
                            new TreeNode(1, new TreeNode(1), new TreeNode(2)),
                            false
                    ),
                    new Case(
                            new TreeNode(1, new TreeNode(2), null),
                            new TreeNode(1, null, new TreeNode(2)),
                            false
                    ),
                    new Case(
                            new TreeNode(1,
                                    new TreeNode(2, new TreeNode(1), new TreeNode(2)),
                                    new TreeNode(3, new TreeNode(2), new TreeNode(4))),
                            new TreeNode(1,
                                    new TreeNode(2, new TreeNode(1), new TreeNode(2)),
                                    new TreeNode(3, new TreeNode(2), new TreeNode(4))),
                            true
                    )
            );
        }
    }
}
