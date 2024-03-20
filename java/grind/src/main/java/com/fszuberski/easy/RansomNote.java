package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 383. Ransom Note
// https://leetcode.com/problems/ransom-note

public class RansomNote {

    public boolean canConstruct(String ransomNote, String magazine) {
        var letterMap = magazine
                .chars()
                .mapToObj(character -> (char) character)
                .collect(Collectors.groupingBy(character -> character, Collectors.counting()));

        for (Character character : ransomNote.toCharArray()) {
            var existingCount = letterMap.get(character);
            if (existingCount == null || existingCount == 0) {
                return false;
            }

            letterMap.put(character, existingCount - 1);
        }

        return true;
    }

    static class RansomNoteTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(String ransomNote, String magazine, boolean expectedResult) {
            final var result = new RansomNote().canConstruct(ransomNote, magazine);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of("a", "b", false),
                    Arguments.of("aa", "ab", false),
                    Arguments.of("aa", "aab", true)
            );
        }
    }
}
