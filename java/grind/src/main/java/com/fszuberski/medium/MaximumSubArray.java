package com.fszuberski.medium;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 53. Maximum Subarray
// https://leetcode.com/problems/maximum-subarray

public class MaximumSubArray {

    // O(n)
    // 1 ms Beats 99.44% of users with Java
    public int maxSubArray(int[] nums) {
        int maxVal = Integer.MIN_VALUE;
        int currentVal = 0;
        int ptr = 0;
        while (ptr < nums.length) {
            currentVal += nums[ptr];
            ptr++;
            if (currentVal > maxVal) {
                maxVal = currentVal;
            }
            if (currentVal < 0) {
                currentVal = 0;
            }
        }

        return maxVal;
    }

    static class MaximumSubArrayTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(int[] nums, int expectedResult) {
            final var result = new MaximumSubArray().maxSubArray(nums);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4}, 6),
                    Arguments.of(new int[]{1}, 1),
                    Arguments.of(new int[]{5, 4, -1, 7, 8}, 23),
                    Arguments.of(new int[]{-1}, -1)
            );
        }
    }
}
