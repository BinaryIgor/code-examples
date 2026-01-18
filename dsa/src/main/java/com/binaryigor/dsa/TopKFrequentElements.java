package com.binaryigor.dsa;

import java.util.*;

public class TopKFrequentElements {

    static int[] topKFrequent(int[] nums, int k) {
        var numsFrequencies = new HashMap<Integer, Integer>();

        for (int num : nums) {
            numsFrequencies.merge(num, 1, Integer::sum);
        }

        var frequenciesNums = new HashMap<Integer, LinkedList<Integer>>();
        var maxHeap = new PriorityQueue<Integer>(Comparator.reverseOrder());

        numsFrequencies.forEach((num, frequency) -> {
            maxHeap.add(frequency);
            frequenciesNums.computeIfAbsent(frequency, $ -> new LinkedList<>()).add(num);
        });

        var result = new int[k];
        for (int i = 0; i < k; i++) {
            var freq = maxHeap.poll();
            result[i] = frequenciesNums.get(freq).pollFirst();
        }
        return result;
    }

    record Case(int[] nums, int k, int[] expected) {

        static List<Case> cases() {
            return List.of(
                    new Case(new int[]{1, 1, 1, 2, 2, 3}, 2, new int[]{1, 2}),
                    new Case(new int[]{1}, 1, new int[]{1}),
                    new Case(new int[]{1, 2, 1, 2, 1, 2, 3, 1, 3, 2}, 2, new int[]{1, 2})
            );
        }

        @Override
        public String toString() {
            return "Case[nums=%s, k=%d,expected=%s]".formatted(Arrays.toString(nums), k, Arrays.toString(expected));
        }
    }
}
