package com.binaryigor.dsa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {

    static final int UNREACHABLE = Integer.MAX_VALUE;

    static List<Integer> shortestDistances(List<List<Edge>> graph, int source) {
        if (graph.isEmpty()) {
            return List.of();
        }

        int n = graph.size();

        var distances = new ArrayList<Integer>(n);
        for (var i = 0; i < n; i++) {
            distances.add(UNREACHABLE);
        }
        distances.set(source, 0);

        var visited = new boolean[n];

        var heap = new PriorityQueue<Node>(Comparator.comparingInt(a -> a.distance));
        heap.add(new Node(source, 0));

        while (!heap.isEmpty()) {
            var current = heap.poll();
            var u = current.vertex;

            if (visited[u]) {
                continue;
            }

            visited[u] = true;

            for (var e : graph.get(u)) {
                var v = e.to;
                var weight = e.weight;

                if (!visited[v] && distances.get(u) != UNREACHABLE) {
                    var newDist = distances.get(u) + weight;
                    if (newDist < distances.get(v)) {
                        distances.set(v, newDist);
                        heap.add(new Node(v, newDist));
                    }
                }
            }
        }

        return distances;
    }

    record Node(int vertex, int distance) {
    }

    record Edge(int to, int weight) {
    }

    record Case(List<List<Edge>> graph, int source, List<Integer> expected) {

        static List<Case> cases() {
            // 0
            var singleNodeG = newGraph(1);
            var singleNodeD = List.of(0);

            // 0 --1--> 1 --2--> 2 --3--> 3
            var simpleLineG = newGraph(4);
            addEdge(simpleLineG, 0, 1, 1);
            addEdge(simpleLineG, 1, 2, 2);
            addEdge(simpleLineG, 2, 3, 3);
            var simpleLineD = List.of(0, 1, 3, 6);

            // 0 --10--> 1
            // 0 --1-->  2 --1--> 1
            var triangleWithAlternativePathG = newGraph(3);
            addEdge(triangleWithAlternativePathG, 0, 1, 10);
            addEdge(triangleWithAlternativePathG, 0, 2, 1);
            addEdge(triangleWithAlternativePathG, 2, 1, 1);
            var triangleWithAlternativePathD = List.of(0, 2, 1);

            var triangleWithAlternativePathDS2 = List.of(UNREACHABLE, 1, 0);

            // 0 --5--> 1 2
            var disconnectedG = newGraph(3);
            addEdge(disconnectedG, 0, 1, 5);
            var disconnectedD = List.of(0, 5, UNREACHABLE);

            // 0 --1--> 1 --1--> 2 --1--> 0
            var cyclesG = newGraph(3);
            addEdge(cyclesG, 0, 1, 1);
            addEdge(cyclesG, 1, 2, 1);
            addEdge(cyclesG, 2, 0, 1);
            var cyclesD = List.of(0, 1, 2);

            // 0 --10--> 1
            // 0 --5--> 1
            var multipleEdgesG = newGraph(2);
            addEdge(multipleEdgesG, 0, 1, 10);
            addEdge(multipleEdgesG, 0, 1, 5);
            var multipleEdgesD = List.of(0, 5);

            // 0 --4--> 1
            // 0 --1--> 2
            // 2 --2--> 1
            // 1 --1--> 3
            // 2 --5--> 3
            var largerG = newGraph(4);
            addEdge(largerG, 0, 1, 4);
            addEdge(largerG, 0, 2, 1);
            addEdge(largerG, 2, 1, 2);
            addEdge(largerG, 1, 3, 1);
            addEdge(largerG, 2, 3, 5);
            var largerD = List.of(0, 3, 1, 4);

            //      1
            //      |
            // 2 -- 0 -- 3
            //      |
            //      4
            var starG = newGraph(5);
            addEdge(starG, 0, 1, 1);
            addEdge(starG, 0, 2, 1);
            addEdge(starG, 0, 3, 1);
            addEdge(starG, 0, 4, 1);
            var starD = List.of(0, 1, 1, 1, 1);

            // 0 --10--> 1
            // 0 --6--> 2
            // 1 --2--> 0
            // 1 --12--> 2
            // 2 --1--> 0
            // 2 --1--> 1
            var denseG = newGraph(3);
            addEdge(denseG, 0, 1, 10);
            addEdge(denseG, 0, 2, 6);
            addEdge(denseG, 1, 0, 2);
            addEdge(denseG, 1, 2, 12);
            addEdge(denseG, 2, 0, 1);
            addEdge(denseG, 2, 1, 1);
            var denseD = List.of(0, 7, 6);

            return List.of(
                    new Case(singleNodeG, 0, singleNodeD),
                    new Case(simpleLineG, 0, simpleLineD),
                    new Case(triangleWithAlternativePathG, 0, triangleWithAlternativePathD),
                    new Case(triangleWithAlternativePathG, 2, triangleWithAlternativePathDS2),
                    new Case(disconnectedG, 0, disconnectedD),
                    new Case(cyclesG, 0, cyclesD),
                    new Case(multipleEdgesG, 0, multipleEdgesD),
                    new Case(largerG, 0, largerD),
                    new Case(starG, 0, starD),
                    new Case(denseG, 0, denseD)
            );
        }

        private static List<List<Edge>> newGraph(int n) {
            var graph = new ArrayList<List<Edge>>();
            for (var i = 0; i < n; i++) {
                graph.add(new ArrayList<>());
            }
            return graph;
        }

        private static void addEdge(List<List<Edge>> graph, int from, int to, int weight) {
            graph.get(from).add(new Edge(to, weight));
        }
    }
}
