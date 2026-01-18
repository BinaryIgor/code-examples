package com.binaryigor.dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LongestIncreasingSubsequence {

    static int length(int[] nums) {
        var longestSubsequences = new ArrayList<Integer>();
        for (int num : nums) {
            int geIdx = fgeBinarySearch(longestSubsequences, num);
            if (geIdx < 0) {
                longestSubsequences.add(num);
            } else {
                longestSubsequences.set(geIdx, num);
            }
        }

        return longestSubsequences.size();
    }

    private static int fgeBinarySearch(ArrayList<Integer> items, int item) {
        int low = 0;
        int high = items.size() - 1;

        int firstFound = -1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int midVal = items.get(mid);
            if (midVal >= item) {
                high = mid - 1;
                firstFound = mid;
            } else {
                low = mid + 1;
            }
        }

        return firstFound;
    }

    record Case(int[] nums, int output) {

        static List<Case> cases() {
            return List.of(
                    new Case(new int[]{10, 9, 2, 5, 3, 7, 101, 18}, 4),
                    new Case(new int[]{18, 55, 66, 2, 3, 54}, 3),
                    new Case(new int[]{0, 1, 0, 3, 2, 3}, 4),
                    new Case(new int[]{7, 7, 7, 7, 7, 7}, 1)
            );
        }

        @Override
        public String toString() {
            return "Case{" +
                    "nums=" + Arrays.toString(nums) +
                    ", expected=" + output +
                    '}';
        }
    }
}
