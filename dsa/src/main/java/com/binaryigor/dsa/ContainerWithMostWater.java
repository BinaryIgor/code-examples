package com.binaryigor.dsa;

import java.util.List;

public class ContainerWithMostWater {

    static int maxArea(int[] height) {
        int i = 0, j = height.length - 1;
        int maxArea = 0;
        while (i != j) {
            var left = height[i];
            var right = height[j];
            var area = (j - i) * Math.min(left, right);
            if (area > maxArea) {
                maxArea = area;
            }
            if (right > left) {
                i++;
            } else {
                j--;
            }

        }
        return maxArea;
    }

    record Case(int[] height, int area) {

        static List<Case> cases() {
            return List.of(
                    new Case(new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7}, 49),
                    new Case(new int[]{1, 1}, 1)
            );
        }
    }
}
