package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 217. Contains Duplicate
// https://leetcode.com/problems/contains-duplicate

public class ContainsDuplicate {

    public boolean containsDuplicate(int[] nums) {
        Set<Integer> encounteredNums = new HashSet<>();
        for (Integer num : nums) {
            if (encounteredNums.contains(num)) {
                return true;
            }
            encounteredNums.add(num);
        }

        return false;
    }


    static class ContainsDuplicateTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(int[] nums, boolean expectedResult) {
            final var result = new ContainsDuplicate().containsDuplicate(nums);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {

            return Stream.of(
                    Arguments.of(new int[]{1, 2, 3, 1}, true),
                    Arguments.of(new int[]{1, 2, 3, 4}, false),
                    Arguments.of(new int[]{1, 1, 1, 3, 3, 4, 3, 2, 4, 2}, true)
            );
        }
    }

}
