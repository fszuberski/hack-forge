package com.fszuberski.leetcode.medium;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 3. Longest Substring Without Repeating Characters
// https://leetcode.com/problems/longest-substring-without-repeating-characters/

public class LongestSubstringWithoutRepeatingCharacters {
    public int lengthOfLongestSubstring(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }

        int left = 0;
        int right = 0;
        int count = 0;
        int result = 0;
        var characters = new HashSet<Character>();
        while (right < s.length()) {
            var currentRight = s.charAt(right);
            if (!characters.contains(currentRight)) {
                characters.add(currentRight);
                right++;
                count++;
                if (count > result) {
                    result = count;
                }
            } else {
                var currentLeft = s.charAt(left);
                while (characters.contains(currentLeft)) {
                    characters.remove(currentLeft);
                    left++;
                    count--;
                }
            }
        }

        return result;
    }

    static class LongestSubstringWithoutRepeatingCharactersTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(String s, int expectedResult) {
            final var result = new LongestSubstringWithoutRepeatingCharacters().lengthOfLongestSubstring(s);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of("abcabcbb", 3),
                    Arguments.of("bbbbb", 1),
                    Arguments.of("pwwkew", 3),
                    Arguments.of("dvdf", 3)
            );
        }
    }
}
