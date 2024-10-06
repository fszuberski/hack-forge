package com.fszuberski.leetcode.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 67. Add Binary
// https://leetcode.com/problems/add-binary

public class AddBinary {
    private static final int ZERO = 48;
    private static final int TWO_ZEROS = ZERO * 2;

    public String addBinary(String a, String b) {
        if (a.isEmpty() && b.isEmpty()) {
            return "";
        }

        var aPtr = a.length() - 1;
        var bPtr = b.length() - 1;

        var next = 0;
        StringBuilder sb = new StringBuilder();
        while (aPtr >= 0 || bPtr >= 0 || next > 0) {
            var sum = sum(
                    aPtr >= 0 ? a.charAt(aPtr) : '0',
                    bPtr >= 0 ? b.charAt(bPtr) : '0')
                    + next;

            aPtr--;
            bPtr--;
            if (sum == 0) {
                sb.append("0");
                next = 0;
            } else if (sum == 1) {
                sb.append("1");
                next = 0;
            } else if (sum == 2) {
                sb.append("0");
                next = 1;
            } else if (sum == 3) {
                sb.append("1");
                next = 1;
            }
        }

        return sb.reverse().toString();
    }

    private int sum(char... values) {
        return new String(values).chars().sum() - TWO_ZEROS;
    }

    static class AddBinaryTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(String a, String b, String expectedResult) {
            final var result = new AddBinary().addBinary(a, b);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of("11", "1", "100"),
                    Arguments.of("1010", "1011", "10101"),
                    Arguments.of("", "", "")
            );
        }
    }
}
