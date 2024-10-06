package com.fszuberski.leetcode.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 278. First Bad Version
// https://leetcode.com/problems/first-bad-version

public class FirstBadVersion {
    private final int firstBadVersion;

    public FirstBadVersion(int firstBadVersion) {
        this.firstBadVersion = firstBadVersion;
    }

    boolean isBadVersion(int version) {
        return version >= firstBadVersion;
    }

    // even more optimised binary search - no additional calls to left and right
    public int firstBadVersion(int n) {
        var low = 1;
        var high = n;
        while (low <= high) {
            var mid = low + (high - low) / 2;

            if (isBadVersion(mid)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }

    // optimised - binary search
/*
    public int firstBadVersion(int n) {
        var low = 1;
        var high = n;

        while (low <= high) {
            var mid = low + (high - low) / 2;
            var left = mid - 1;
            var right = mid + 1;

            if (isBadVersion(mid)) {
                if (!isBadVersion(left)) {
                    return mid;
                } else {
                    if (mid != high) {
                        high = mid;
                    } else {
                        high--;
                    }
                }
            } else if (isBadVersion(right)) {
                return right;
            } else {
                if (mid != low) {
                    low = mid;
                } else {
                    low++;
                }
            }
        }

        return -1;
    }
*/


    // brute force
/*
    public int firstBadVersion(int n) {
        return IntStream.rangeClosed(1, n)
                .mapToObj(version -> new BadVersionPair(version, isBadVersion(version)))
                .filter(badVersionPair -> badVersionPair.isBad)
                .findFirst()
                .map(badVersionPair -> badVersionPair.version)
                .orElse(-1);
    }

    private record BadVersionPair(int version, boolean isBad){}
*/

    static class FirstBadVersionTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(int maxVersion, int expectedResult) {
            final var result = new FirstBadVersion(expectedResult).firstBadVersion(maxVersion);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(5, 4),
                    Arguments.of(1, 1),
                    Arguments.of(2, 1),
                    Arguments.of(3, 3),
                    Arguments.of(2126753390, 1702766719)
            );
        }
    }
}
