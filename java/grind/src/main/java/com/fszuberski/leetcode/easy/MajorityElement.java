package com.fszuberski.leetcode.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 169. Majority Element
// https://leetcode.com/problems/majority-element

public class MajorityElement {

    // O(1) memory version
    public int majorityElement(int[] nums) {
        var result = -1;
        var count = 0;

        for (int num : nums) {
            if (count == 0) {
                result = num;
                count++;
            } else if (result == num) {
                count++;
            } else {
                count--;
            }
        }

        return result;
    }

    static class MajorityElementTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(int[] nums, int expectedResult) {
            final var result = new MajorityElement().majorityElement(nums);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {

            return Stream.of(
                    Arguments.of(new int[]{3, 2, 3}, 3),
                    Arguments.of(new int[]{2, 2, 1, 1, 1, 2, 2}, 2),
                    Arguments.of(new int[]{6, 5, 5}, 5)
            );
        }
    }
}
