package com.binaryigor.dsa;

import java.util.*;
import java.util.stream.Collectors;

public class CourseSchedule {

    static boolean canFinish(int numCourses, int[][] prerequisites) {
        if (prerequisites.length == 0) {
            return true;
        }

        var graph = new HashMap<Integer, Set<Integer>>();
        for (int i = 0; i < numCourses; i++) {
            graph.put(i, new HashSet<>());
        }
        var edgesToNodes = new HashMap<Integer, Set<Integer>>();

        for (int[] nodeWithPotentialEdge : prerequisites) {
            var node = nodeWithPotentialEdge[0];
            var nodeEdges = graph.get(node);
            if (nodeWithPotentialEdge.length > 1) {
                var nodeEdge = nodeWithPotentialEdge[1];
                nodeEdges.add(nodeEdge);

                var edgeNodes = edgesToNodes.computeIfAbsent(nodeEdge, $ -> new HashSet<>());
                edgeNodes.add(node);
            }
        }

        var independentNodes = graph.entrySet().stream()
                .filter(e -> e.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!independentNodes.isEmpty()) {
            var independentNode = independentNodes.remove();
            var dependentNodes = edgesToNodes.remove(independentNode);
            if (dependentNodes != null) {
                dependentNodes.forEach(n -> {
                    var nodeDeps = graph.get(n);
                    nodeDeps.remove(independentNode);
                    if (nodeDeps.isEmpty()) {
                        graph.remove(n);
                        independentNodes.add(n);
                    }
                });
            }

            graph.remove(independentNode);
        }

        return graph.isEmpty();
    }

    record Case(int courses, int[][] prerequisites, boolean canFinish) {

        static List<Case> cases() {
            return List.of(
                    new Case(8, new int[][]{{1, 0}, {2, 6}, {3, 0}, {1, 7}, {5, 1}, {6, 4}, {7, 0}}, true),
                    new Case(2, new int[][]{{1, 0}}, true),
                    new Case(2, new int[][]{{1, 0}, {0, 1}}, false),
                    new Case(3, new int[][]{{1, 0}, {1}, {1, 2}}, true),
                    new Case(4, new int[][]{{0}, {1}, {2}, {3, 2}}, true),
                    new Case(3, new int[][]{{0}, {0, 1}, {1, 2}}, true),
                    new Case(1, new int[][]{}, true),
                    new Case(5, new int[][]{{1, 4}, {2, 4}, {3, 1}, {3, 2}}, true),
                    new Case(7, new int[][]{{1, 0}, {0, 3}, {0, 2}, {3, 2}, {2, 5}, {4, 5}, {5, 6}, {2, 4}}, true)
            );
        }
    }
}
