package com.fszuberski.leetcode.medium;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 15. 3Sum
// https://leetcode.com/problems/3sum

public class ThreeSum {

    // O(nlogn) + O(n^2)
    // but with optimisations - skipping iterations if neighbouring numbers are the same;
    // this also allows the use of a list directly instead of a set
    // Runtime 32ms Beats 56.55% of users with Java
    public List<List<Integer>> threeSum(int[] nums) {
        if (nums.length < 3) {
            return List.of();
        }

        // O(nlogn)
        Arrays.sort(nums);
        var result = new ArrayList<List<Integer>>();
        for (var left = 0; left < nums.length - 2; left++) {
            if (left > 0 && nums[left] == nums[left - 1]) {
                continue;
            }

            var mid = left + 1;
            var right = nums.length - 1;

            while (mid < right) {
                var sum = nums[left] + nums[mid] + nums[right];
                if (sum == 0) {
                    result.add(List.of(nums[left], nums[mid], nums[right]));
                    while (mid < right && nums[mid] == nums[mid + 1]) {
                        mid++;
                    }
                    while (mid < right && nums[right] == nums[right - 1]) {
                        right--;
                    }
                    mid++;
                    right--;
                } else if (sum > 0) {
                    right--;
                } else {
                    mid++;
                }
            }
        }

        return result;
    }

    // O(nlogn) + O(n^2)
    // Runtime 1023ms Beats 7.14% of users with Java
    public List<List<Integer>> threeSum_slow(int[] nums) {
        if (nums.length < 3) {
            return List.of();
        }

        // O(nlogn)
        Arrays.sort(nums);
        var result = new HashSet<List<Integer>>();

        // O(n)
        for (var left = 0; left < nums.length - 2; left++) {
            var mid = left + 1;
            var right = nums.length - 1;

            // O(n)
            while (mid < right) {
                var sum = nums[left] + nums[mid] + nums[right];
                if (sum == 0) {
                    result.add(List.of(nums[left], nums[mid], nums[right]));
                    mid++;
                    right--;
                } else if (sum > 0) {
                    right--;
                } else {
                    mid++;
                }
            }
        }

        return result.stream().toList();
    }

    // O(n^3)
    public List<List<Integer>> threeSum_slowest(int[] nums) {
        if (nums.length < 3) {
            return List.of();
        }

        var result = new HashSet<List<Integer>>();
        for (var i = 0; i < nums.length - 2; i++) {
            for (var j = 1; j < nums.length - 1; j++) {
                for (var k = 2; k < nums.length; k++) {
                    if (i != j && i != k && j != k &&
                            nums[i] + nums[j] + nums[k] == 0) {
                        result.add(
                                Stream.of(nums[i], nums[j], nums[k])
                                        .sorted()
                                        .collect(Collectors.toList())
                        );
                    }
                }
            }
        }

        return result.stream().toList();
    }

    static class ThreeSumTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(int[] nums, List<List<Integer>> expectedResult) {
            final var result = new ThreeSum().threeSum(nums);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            new int[]{-1, 0, 1, 2, -1, -4},
                            List.of(List.of(-1, -1, 2), List.of(-1, 0, 1))),
                    Arguments.of(
                            new int[]{0, 1, 1},
                            List.of()),
                    Arguments.of(
                            new int[]{0, 0, 0},
                            List.of(List.of(0, 0, 0))),
                    Arguments.of(
                            new int[]{1, 2, -2, -1},
                            List.of()),
                    Arguments.of(
                            new int[]{-1, 0, 1, 0},
                            List.of(List.of(-1, 0, 1))),
                    Arguments.of(
                            new int[]{-2, 0, 1, 1, 2},
                            List.of(List.of(-2, 0, 2), List.of(-2, 1, 1)))
            );
        }
    }
}

