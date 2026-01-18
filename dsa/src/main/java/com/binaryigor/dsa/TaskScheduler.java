package com.binaryigor.dsa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskScheduler {

    static int leastInterval(char[] tasks, int n) {
        var taskFrequencies = new HashMap<Character, Integer>();
        for (var t : tasks) {
            taskFrequencies.merge(t, 1, Integer::sum);
        }

        var sortedFrequencies = taskFrequencies.entrySet().stream()
                .map(t -> new TaskFrequency(t.getKey(), t.getValue()))
                .sorted((a, b) -> -Integer.compare(a.frequency, b.frequency))
                .toList();

        var optimalSchedule = new ArrayList<Character>();

        var gapsQueue = new ArrayDeque<Integer>();
        for (var s : sortedFrequencies) {
            var task = s.task;
            var frequency = s.frequency;

            if (optimalSchedule.isEmpty()) {
                for (int i = 1; i <= frequency; i++) {
                    optimalSchedule.add(task);
                    if (i == frequency) {
                        continue;
                    }
                    for (int j = 0; j < n; j++) {
                        optimalSchedule.add(null);
                    }
                }

                for (int i = 1; i <= n; i++) {
                    // Example for frequency = 3 & n = 3.
                    // Input: A, null, null, A, null, null, A, null, null, A
                    // Gaps after iteration 1: [1, 4, 7]
                    // Gaps after iteration 2: [1, 4, 7, 2, 5, 8]
                    for (int j = 0; j < (frequency - 1); j++) {
                        var gapIdx = i + (j * (n + 1));
                        gapsQueue.add(gapIdx);
                    }
                }
            } else {
                for (int i = 0; i < frequency; i++) {
                    var gapIdx = gapsQueue.peek();
                    if (gapIdx == null) {
                        // no more gaps, insert at the end
                        optimalSchedule.add(task);
                    } else {
                        var allowedGap = true;
                        int j = gapIdx - 1;
                        var checks = 0;
                        while (checks < n && j >= 0) {
                            var t = optimalSchedule.get(j);
                            if (t != null && t == task) {
                                allowedGap = false;
                                break;
                            }

                            checks++;
                            j--;
                        }
                        if (allowedGap) {
                            optimalSchedule.set(gapIdx, task);
                            gapsQueue.removeFirst();
                        } else {
                            optimalSchedule.add(task);
                        }
                    }
                }
            }
        }

        return optimalSchedule.size();
    }

    private record TaskFrequency(char task, int frequency) {
    }

    record Case(char[] tasks, int n, int intervals) {

        static List<Case> cases() {
            return List.of(
                    new Case(new char[]{'A', 'A', 'A', 'B', 'B', 'B'}, 2, 8),
                    new Case(new char[]{'A', 'C', 'A', 'B', 'D', 'B'}, 1, 6),
                    new Case(new char[]{'A', 'A', 'A', 'B', 'B', 'B'}, 3, 10),
                    new Case(new char[]{'A', 'A', 'A'}, 2, 7),
                    // is: A, B, C, D, G, A, null, A, null, A
                    // should be: A, B, A, C, A, D, A, G
                    new Case(new char[]{'B', 'C', 'D', 'A', 'A', 'A', 'A', 'G'}, 1, 8),
                    new Case(new char[]{'B', 'A', 'B', 'A', 'C'}, 3, 6),
                    new Case(new char[]{'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E'}, 4, 10)
            );
        }

    }
}
