package com.binaryigor.dsa;

import java.util.*;

public class BinaryTreeLevelOrderTraversal {

    static List<List<Integer>> levelOrder(TreeNode root) {
        if (root == null) {
            return List.of();
        }

        var queue = new LinkedList<TreeNode>();
        var nodesLevels = calculateNodesLevels(root);
        var traversal = new ArrayList<List<Integer>>();

        queue.add(root);

        int currentLevel = 0;
        var currentTraversal = new ArrayList<Integer>();

        while (!queue.isEmpty()) {
            var node = queue.pop();

            var nodeLevel = nodesLevels.get(node);
            if (nodeLevel != currentLevel) {
                currentLevel = nodeLevel;
                traversal.add(currentTraversal);
                currentTraversal = new ArrayList<>();
            }

            currentTraversal.add(node.val);

            if (node.left != null) {
                queue.add(node.left);
            }
            if (node.right != null) {
                queue.add(node.right);
            }
        }

        if (!currentTraversal.isEmpty()) {
            traversal.add(currentTraversal);
        }

        return traversal;
    }

    static Map<TreeNode, Integer> calculateNodesLevels(TreeNode root) {
        var nodeLevels = new HashMap<TreeNode, Integer>();
        calculateNodesLevels(root, nodeLevels, 0);
        return nodeLevels;
    }

    private static void calculateNodesLevels(TreeNode node,
                                             Map<TreeNode, Integer> nodeLevels,
                                             int currentHeight) {
        nodeLevels.put(node, currentHeight);
        if (node.left != null) {
            calculateNodesLevels(node.left, nodeLevels, currentHeight + 1);
        }
        if (node.right != null) {
            calculateNodesLevels(node.right, nodeLevels, currentHeight + 1);
        }
    }

    record Case(TreeNode tree, List<List<Integer>> expectedLevelOrderTraversal) {
        static List<Case> cases() {
            return List.of(
                    new Case(new TreeNode(3,
                            new TreeNode(9, new TreeNode(5), new TreeNode(12)),
                            new TreeNode(20,
                                    new TreeNode(15), new TreeNode(7))),
                            List.of(
                                    List.of(3),
                                    List.of(9, 20),
                                    List.of(5, 12, 15, 7)
                            )),
                    new Case(new TreeNode(3,
                            new TreeNode(9),
                            new TreeNode(20,
                                    new TreeNode(15), new TreeNode(7))),
                            List.of(
                                    List.of(3),
                                    List.of(9, 20),
                                    List.of(15, 7)
                            )),
                    new Case(null, List.of())
            );
        }

    }
}
