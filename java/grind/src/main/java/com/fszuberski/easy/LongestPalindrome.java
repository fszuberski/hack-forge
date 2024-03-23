package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 409. Longest Palindrome
// https://leetcode.com/problems/longest-palindrome

public class LongestPalindrome {

    // 1 ms Beats 100.00% of users with Java
    public int longestPalindrome(String s) {
        // s consists of lowercase and/or uppercase English letters only.
        // ASCII:
        // Each character is associated with a unique 7-bit code, ranging from 0 to 127.
        // The ASCII values of the alphabet vary from 65 to 90 for uppercase letters and from 97 to 122 for lowercase letters.
        //
        // avoiding autoboxing by using an array instead of a generic container
        var charCountArray = new int[128]; // could be optimised further by shortening the array...
        for (char character : s.toCharArray()) {
            charCountArray[character] = charCountArray[character] + 1;
        }

        var result = 0;
        var hasOddEntry = false;
        for (int charCount : charCountArray) {
            if (charCount % 2 != 0) {
                hasOddEntry = true;
                result += charCount - 1;
            } else {
                result += charCount;
            }
        }

        return result + (hasOddEntry ? 1 : 0);
    }

    // 6 ms Beats 59.16% of users with Java
    public int longestPalindrome_map(String s) {
        var map = new HashMap<Character, Integer>();
        for (Character character : s.toCharArray()) {
            map.put(character, map.getOrDefault(character, 0) + 1);
        }

        var result = 0;
        var hasOddEntry = false;
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            if (entry.getValue() % 2 != 0) {
                hasOddEntry = true;
                result += entry.getValue() - 1;
            } else {
                result += entry.getValue();
            }

        }

        return result + (hasOddEntry ? 1 : 0);
    }

    // 11 ms Beats 7.53% of users with Java
    public int longestPalindrome_stream(String s) {
        var map = s
                .chars()
                .mapToObj(character -> (char) character)
                .collect(Collectors.groupingBy(character -> character, Collectors.counting()));

        var result = 0L;
        var hasOddEntry = false;
        for (Map.Entry<Character, Long> entry : map.entrySet()) {
            if (entry.getValue() % 2 != 0) {
                hasOddEntry = true;
                result += entry.getValue() - 1;
            } else {
                result += entry.getValue();
            }

        }

        return (int) (result + (hasOddEntry ? 1 : 0));
    }

    static class LongestPalindromeTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(String s, int expectedResult) {
            final var result = new LongestPalindrome().longestPalindrome(s);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {

            return Stream.of(
                    Arguments.of("abccccdd", 7),
                    Arguments.of("a", 1),
                    Arguments.of("ab", 1),
                    Arguments.of("aabb", 4)
            );
        }
    }
}
