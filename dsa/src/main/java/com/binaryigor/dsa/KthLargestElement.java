package com.binaryigor.dsa;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class KthLargestElement {

    static int find(int[] nums, int k) {
        var heap = new PriorityQueue<Integer>(Comparator.reverseOrder());
        for (var n : nums) {
            heap.add(n);
        }

        if (k > heap.size()) {
            return -1;
        }

        var kLargest = -1;
        for (int i = 0; i < k; i++) {
            kLargest = heap.remove();
        }

        return kLargest;
    }

    record Case(int[] nums, int k, int expected) {
        static List<Case> cases() {
            return List.of(
                    new Case(new int[]{3, 2, 1, 5, 6, 4}, 2, 5),
                    new Case(new int[]{3, 2, 3, 1, 2, 4, 5, 5, 6}, 4, 4)
            );
        }
    }
}
