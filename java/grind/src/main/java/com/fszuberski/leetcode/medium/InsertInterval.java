package com.fszuberski.leetcode.medium;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

// 57. Insert Interval
// https://leetcode.com/problems/insert-interval

public class InsertInterval {

    // O(n)
    public int[][] insert(int[][] intervals, int[] newInterval) {
        var result = new ArrayList<int[]>();

        var inserted = false;
        var left = Integer.MAX_VALUE;
        var right = Integer.MIN_VALUE;
        for (int i = 0; i < intervals.length; i++) {
            if (areOverlappingIntervals(intervals[i], newInterval)) {
                left = min(intervals[i][0], newInterval[0], left);
                right = max(intervals[i][1], newInterval[1], right);
            } else {
                if (left != Integer.MAX_VALUE && right != Integer.MIN_VALUE) {
                    result.add(new int[]{left, right});
                    result.add(intervals[i]);
                    left = Integer.MAX_VALUE;
                    right = Integer.MIN_VALUE;
                    inserted = true;
                } else if (!inserted && newInterval[1] < intervals[i][0]) {
                    result.add(newInterval);
                    result.add(intervals[i]);
                    inserted = true;
                } else if (!inserted && i == intervals.length - 1 && newInterval[0] > intervals[i][1]) {
                    result.add(intervals[i]);
                    result.add(newInterval);
                    inserted = true;
                } else {
                    result.add(intervals[i]);
                }
            }
        }

        if (left != Integer.MAX_VALUE && right != Integer.MIN_VALUE) {
            result.add(new int[]{left, right});
            inserted = true;
        }

        if (!inserted) {
            result.add(newInterval);
        }

        return result.toArray(new int[][]{});
    }

    private boolean areOverlappingIntervals(int[] interval1, int[] interval2) {
        return interval1[0] <= interval2[1] && interval2[0] <= interval1[1];
    }

    private int min(int... nums) {
        return Arrays.stream(nums).min().orElse(-1);
    }

    private int max(int... nums) {
        return Arrays.stream(nums).max().orElse(-1);
    }

    static class InsertIntervalTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(int[][] intervals, int[] newInterval, int[][] expectedResult) {
            final var result = new InsertInterval().insert(intervals, newInterval);
            assertArrayEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            new int[][]{new int[]{1, 3}, new int[]{6, 9}},
                            new int[]{2, 5},
                            new int[][]{new int[]{1, 5}, new int[]{6, 9}}
                    ),
                    Arguments.of(
                            new int[][]{new int[]{1, 2}, new int[]{3, 5}, new int[]{6, 7}, new int[]{8, 10}, new int[]{12, 16}},
                            new int[]{4, 8},
                            new int[][]{new int[]{1, 2}, new int[]{3, 10}, new int[]{12, 16}}
                    ),
                    Arguments.of(
                            new int[][]{new int[]{1, 3}},
                            new int[]{1, 3},
                            new int[][]{new int[]{1, 3}}
                    ),
                    Arguments.of(
                            new int[][]{},
                            new int[]{5, 7},
                            new int[][]{new int[]{5, 7}}
                    ),
                    Arguments.of(
                            new int[][]{new int[]{1, 5}},
                            new int[]{2, 3},
                            new int[][]{new int[]{1, 5}}
                    ),
                    Arguments.of(
                            new int[][]{new int[]{3, 5}, new int[]{12, 15}},
                            new int[]{6, 6},
                            new int[][]{new int[]{3, 5}, new int[]{6, 6}, new int[]{12, 15}}
                    ),
                    Arguments.of(
                            new int[][]{new int[]{2, 5}, new int[]{6, 7}, new int[]{8, 9}},
                            new int[]{0, 1},
                            new int[][]{new int[]{0, 1}, new int[]{2, 5}, new int[]{6, 7}, new int[]{8, 9}}
                    )

            );
        }
    }
}
