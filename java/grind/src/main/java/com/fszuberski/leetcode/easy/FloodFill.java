package com.fszuberski.leetcode.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

// 733. Flood Fill
// https://leetcode.com/problems/flood-fill/

public class FloodFill {
    public int[][] floodFill(int[][] image, int sr, int sc, int color) {
        if (image[sr][sc] == color) {
            return image;
        }

        var initialColor = image[sr][sc];
        recursiveFill(image, initialColor, sr, sc, color);

        return image;
    }

    // mutates the original image array
    private void recursiveFill(int[][] image, int initialColor, int sr, int sc, int color) {
        if (withinBounds(image, sr, sc) && image[sr][sc] == initialColor) {
            image[sr][sc] = color;
            recursiveFill(image, initialColor, sr - 1, sc, color);
            recursiveFill(image, initialColor, sr + 1, sc, color);
            recursiveFill(image, initialColor, sr, sc - 1, color);
            recursiveFill(image, initialColor, sr, sc + 1, color);
        }
    }

    private boolean withinBounds(int[][] image, int sr, int sc) {
        return sr >= 0 && sr < image.length &&
                sc >= 0 && sc < image[sr].length;
    }

    static class FloodFillTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(int[][] image, int sr, int sc, int color, int[][] expectedResult) {
            final var result = new FloodFill().floodFill(image, sr, sc, color);
            assertTrue(Arrays.deepEquals(expectedResult, result));
        }

        private static Stream<Arguments> testCases() {

            return Stream.of(
                    // Input: image = [[1,1,1],[1,1,0],[1,0,1]], sr = 1, sc = 1, color = 2
                    // Output: [[2,2,2],[2,2,0],[2,0,1]]
                    Arguments.of(
                            new int[][]{
                                    new int[]{1, 1, 1},
                                    new int[]{1, 1, 0},
                                    new int[]{1, 0, 1}
                            },
                            1, 1, 2,
                            new int[][]{
                                    new int[]{2, 2, 2},
                                    new int[]{2, 2, 0},
                                    new int[]{2, 0, 1}
                            }
                    ),
                    // Input: image = [[0,0,0],[0,0,0]], sr = 0, sc = 0, color = 0
                    // Output: [[0,0,0],[0,0,0]]
                    Arguments.of(

                            new int[][]{
                                    new int[]{0, 0, 0},
                                    new int[]{0, 0, 0}
                            },
                            0, 0, 0,
                            new int[][]{
                                    new int[]{0, 0, 0},
                                    new int[]{0, 0, 0}
                            }
                    )
            );
        }
    }
}
