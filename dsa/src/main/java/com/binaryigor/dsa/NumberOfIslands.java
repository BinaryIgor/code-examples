package com.binaryigor.dsa;

import java.util.List;

public class NumberOfIslands {

    static int number(char[][] grid) {
        boolean[][] checked = new boolean[grid.length][grid[0].length];
        int islands = 0;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                var islandFound = findIsland(grid, i, j, checked, false);
                if (islandFound) {
                    islands++;
                }
            }
        }

        return islands;
    }

    static boolean findIsland(char[][] grid,
                              int startRow,
                              int startCol,
                              boolean[][] checked,
                              boolean islandFound) {
        if (checked[startRow][startCol]) {
            return islandFound;
        }

        checked[startRow][startCol] = true;

        if (grid[startRow][startCol] == '0') {
            return islandFound;
        }

        // up
        if (startRow > 0) {
            findIsland(grid, startRow - 1, startCol, checked, true);
        }
        // right
        if (startCol < (grid[0].length - 1)) {
            findIsland(grid, startRow, startCol + 1, checked, true);
        }
        // down
        if (startRow < (grid.length - 1)) {
            findIsland(grid, startRow + 1, startCol, checked, true);
        }
        // left
        if (startCol > 0) {
            findIsland(grid, startRow, startCol - 1, checked, true);
        }

        return true;
    }


    record Case(char[][] grid, int islands) {

        static List<Case> cases() {
            return List.of(
                    new Case(new char[][]{
                            {'1', '1', '1', '1', '0'},
                            {'1', '1', '0', '1', '0'},
                            {'1', '1', '0', '0', '0'},
                            {'0', '0', '0', '0', '0'},
                    }, 1),
                    new Case(new char[][]{
                            {'1', '1', '0', '0', '0'},
                            {'1', '1', '0', '0', '0'},
                            {'0', '0', '1', '0', '0'},
                            {'0', '0', '0', '1', '1'},
                    }, 3)
            );
        }
    }
}
