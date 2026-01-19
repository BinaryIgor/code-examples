package com.binaryigor.dsa;

import java.util.*;

public class NetworkDelayTime {

    static int bestTime(int[][] times, int n, int k) {
        var graph = new HashMap<Integer, List<Edge>>();
        for (var t : times) {
            var from = t[0];
            var to = t[1];
            var weight = t[2];
            graph.computeIfAbsent(from, $ -> new ArrayList<>())
                    .add(new Edge(to, weight));
        }

        var distances = new int[n];
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[k - 1] = 0;

        var visited = new boolean[n];

        var pq = new PriorityQueue<Node>(Comparator.comparing(a -> a.distance));
        pq.add(new Node(k, 0));


        while (!pq.isEmpty()) {
            var current = pq.poll();
            var u = current.vertex;

            if (visited[u - 1]) {
                continue;
            }

            visited[u - 1] = true;

            for (var e : graph.getOrDefault(u, List.of())) {
                var v = e.to;
                var weight = e.weight;

                if (!visited[v - 1] && distances[u - 1] != Integer.MAX_VALUE) {
                    var newDist = distances[u - 1] + weight;
                    if (newDist < distances[v - 1]) {
                        distances[v - 1] = newDist;
                        pq.add(new Node(v, newDist));
                    }
                }
            }
        }

        var maxDist = -1;
        for (var dist : distances) {
            // all nodes must be reachable
            if (dist == Integer.MAX_VALUE) {
                return -1;
            }
            if (dist > maxDist) {
                maxDist = dist;
            }
        }

        return maxDist;
    }

    private record Node(int vertex, int distance) {
    }

    private record Edge(int to, int weight) {
    }

    record Case(int[][] times, int n, int k, int expected) {
        static List<Case> cases() {
            return List.of(
                    new Case(new int[][]{{2, 1, 1}, {2, 3, 1}, {3, 4, 1}}, 4, 2, 2),
                    new Case(new int[][]{{1, 2, 1}}, 2, 1, 1),
                    new Case(new int[][]{{1, 2, 1}}, 2, 2, -1)
            );
        }
    }
}
