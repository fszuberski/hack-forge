package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 704. Binary Search
// https://leetcode.com/problems/binary-search

public class BinarySearch {

    public int search(int[] nums, int target) {
        if (nums.length == 0) {
            return -1;
        }

        int left = 0;
        int right = nums.length - 1;

        while (left < right) {
            int center = (left + right) / 2;

            if (nums[center] == target) {
                return center;
            } else if (nums[center] > target) {
                if (right == center) {
                    break;
                }
                right = center;
            } else if (nums[center] < target) {
                if (left == center) {
                    break;
                }
                left = center;
            }
        }

        if (nums[left] == target) {
            return left;
        }
        if (nums[right] == target) {
            return right;
        }

        return -1;
    }

    static class BinarySearchTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(int[] nums, int target, int expectedResult) {
            final var result = new BinarySearch().search(nums, target);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(new int[]{-1, 0, 3, 5, 9, 12}, 9, 4),
                    Arguments.of(new int[]{-1, 0, 3, 5, 9, 12}, 2, -1),
                    Arguments.of(new int[]{5}, 5, 0),
                    Arguments.of(new int[]{2, 5}, 5, 1),
                    Arguments.of(new int[]{5, 2}, 5, 0)
            );
        }
    }
}
