package com.fszuberski.easy;

// 125. Valid Palindrome
// https://leetcode.com/problems/valid-palindrome/

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidPalindrome {

    public boolean isPalindrome(String string) {
        var left = 0;
        var right = string.length() - 1;

        while (left < right) {
            if (!isAlphanumeric(string.charAt(left))) {
                left++;
                continue;
            }
            if (!isAlphanumeric(string.charAt(right))) {
                right--;
                continue;
            }

            if (!equalCaseInsensitive(string.charAt(left), string.charAt(right))) {
                return false;
            }

            left++;
            right--;
        }

        return true;
    }

    private boolean equalCaseInsensitive(char c1, char c2) {
        return Character.toLowerCase(c1) == Character.toLowerCase(c2);
    }

    private boolean isAlphanumeric(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c);
    }

    // Clearer, but uses more memory
    public boolean isPalindrome_lesserPerformance(String string) {
        final var alphanumeric = string.toLowerCase().replaceAll("[^A-Za-z0-9]", "");
        var left = 0;
        var right = alphanumeric.length() - 1;

        while (left < right) {
            if (alphanumeric.charAt(left) != alphanumeric.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }

        return true;
    }

    static class ValidPalindromeTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(String input, boolean expectedResult) {
            final var result = new ValidPalindrome().isPalindrome(input);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of("A man, a plan, a canal: Panama", true),
                    Arguments.of("race a car", false),
                    Arguments.of(" ", true)
            );
        }
    }
}
