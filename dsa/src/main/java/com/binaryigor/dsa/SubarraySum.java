package com.binaryigor.dsa;

import java.util.Arrays;
import java.util.List;

public class SubarraySum {

    static int sum(int[] nums, int k) {
        var matchingSubarrays = 0;

        for (int i = 0; i < nums.length; i++) {
            var subSum = nums[i];
            if (subSum == k) {
                matchingSubarrays++;
            }

            for (int j = i + 1; j < nums.length; j++) {
                subSum += nums[j];
                if (subSum == k) {
                    matchingSubarrays++;
                }
            }
        }

        return matchingSubarrays;
    }

    record Case(int[] nums, int k, int expectedK) {

        static List<Case> cases() {
            return List.of(
                    new Case(new int[]{1, 1, 1}, 2, 2),
                    new Case(new int[]{1, 2, 3}, 3, 2),
                    new Case(new int[]{-1, -1, 1}, 0, 1)
            );
        }

        @Override
        public String toString() {
            return "Case{" +
                    "nums=" + Arrays.toString(nums) +
                    ", k=" + k +
                    ", expectedK=" + expectedK +
                    '}';
        }
    }
}
