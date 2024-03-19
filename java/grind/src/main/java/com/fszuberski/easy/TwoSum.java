package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

// 1. Two Sum
// https://leetcode.com/problems/two-sum

public class TwoSum {

    public int[] twoSum(int[] nums, int target) {
        var complimentMap = new HashMap<Integer, Integer>();

        for (int i = 0; i < nums.length; i++) {
            var j = complimentMap.get(nums[i]);
            if (j != null) {
                return new int[]{j, i};
            }
            complimentMap.put(target - nums[i], i);
        }

        return new int[]{};
    }

    static class TwoSumTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(int[] nums, int target, int[] expectedResult) {
            final var result = new TwoSum().twoSum(nums, target);
            assertTrue(Arrays.equals(expectedResult, result));
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(new int[]{2, 7, 11, 15}, 9, new int[]{0, 1}),
                    Arguments.of(new int[]{3, 2, 4}, 6, new int[]{1, 2}),
                    Arguments.of(new int[]{3, 3}, 6, new int[]{0, 1})
            );
        }
    }
}
