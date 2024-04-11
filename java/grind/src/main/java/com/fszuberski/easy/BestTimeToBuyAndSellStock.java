package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 121. Best Time to Buy and Sell Stock
// https://leetcode.com/problems/best-time-to-buy-and-sell-stock

public class BestTimeToBuyAndSellStock {

    // O(n)
    // 2 ms Beats 78.71% of users with Java
    public int maxProfit(int[] prices) {
        if (prices.length <= 1) {
            return 0;
        }

        var result = 0;
        var left = 0;
        var right = 1;
        while (right < prices.length) {
            var diff = prices[right] - prices[left];
            if (result < diff) {
                result = diff;
            }
            if (diff <= 0) {
                left = right;
            }
            right++;
        }

        return result;
    }

    static class BestTimeToBuyAndSellStockTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(int[] prices, int expectedResult) {
            final var result = new BestTimeToBuyAndSellStock().maxProfit(prices);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(new int[]{7, 1, 5, 3, 6, 4}, 5),
                    Arguments.of(new int[]{7, 6, 4, 3, 1}, 0),
                    Arguments.of(new int[]{1, 2, 4}, 3),
                    Arguments.of(new int[]{2, 1, 2, 1, 0, 1, 2}, 2),
                    Arguments.of(new int[]{1, 2, 4, 2, 5, 7, 2, 4, 9, 0, 9}, 9)
            );
        }
    }
}
