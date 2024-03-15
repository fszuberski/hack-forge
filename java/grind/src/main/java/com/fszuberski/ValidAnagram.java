package com.fszuberski;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

// 242. Valid Anagram
// https://leetcode.com/problems/valid-anagram

public class ValidAnagram {

    public boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }

        var characterMap = stringToCharStream(s)
                .collect(groupingBy(character -> character, counting()));

        var containsCharacters = stringToCharStream(t).allMatch(character -> {
            var value = characterMap.get(character);
            if (value == null) {
                return false;
            }

            value -= 1;
            if (value == 0) {
                characterMap.remove(character);
            } else {
                characterMap.put(character, value);
            }

            return true;
        });

        return containsCharacters && characterMap.isEmpty();
    }

    private Stream<Character> stringToCharStream(String string) {
        return string.chars()
                .mapToObj(character -> (char) character);
    }

    static class ValidAnagramTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(String input1, String input2, boolean expectedResult) {
            final var result = new ValidAnagram().isAnagram(input1, input2);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of("anagram", "nagaram", true),
                    Arguments.of("rat", "car", false),
                    Arguments.of("ab", "a", false)
            );
        }
    }
}
