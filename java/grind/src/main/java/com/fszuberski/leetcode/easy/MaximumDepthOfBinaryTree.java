package com.fszuberski.leetcode.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 104. Maximum Depth of Binary Tree
// https://leetcode.com/problems/maximum-depth-of-binary-tree

public class MaximumDepthOfBinaryTree {

    int maxDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }

        return Math.max(maxDepth(root.left), maxDepth(root.right)) + 1;
    }

    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    static class MaximumDepthOfBinaryTreeTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(TreeNode root, int expectedResult) {
            final var result = new MaximumDepthOfBinaryTree().maxDepth(root);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            new TreeNode(1,
                                    new TreeNode(9),
                                    new TreeNode(20,
                                            new TreeNode(15),
                                            new TreeNode(7))),
                            3
                    ),
                    Arguments.of(
                            new TreeNode(1,
                                    null,
                                    new TreeNode(2)),
                            2
                    )
            );
        }
    }
}
