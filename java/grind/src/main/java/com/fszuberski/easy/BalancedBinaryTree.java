package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 110. Balanced Binary Tree
// https://leetcode.com/problems/balanced-binary-tree

class BalancedBinaryTree {

    public boolean isBalanced(TreeNode root) {
        return isBalancedHelper(root).isBalanced;
    }

    private Result isBalancedHelper(TreeNode root) {
        if (root == null) {
            return new Result(true, 0);
        }

        var leftResult = isBalancedHelper(root.left);
        var rightResult = isBalancedHelper(root.right);
        var heightDifference = Math.abs(leftResult.height - rightResult.height);

        var balanced = leftResult.isBalanced && rightResult.isBalanced && heightDifference <= 1;
        var currentHeight = Math.max(leftResult.height, rightResult.height) + 1;

        return new Result(balanced, currentHeight);
    }

    private record Result(boolean isBalanced, int height) {
    }

    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode() {
        }

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    static class BalancedBinaryTreeTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(TreeNode root, boolean expectedResult) {
            final var result = new BalancedBinaryTree().isBalanced(root);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            new TreeNode(3,
                                    new TreeNode(9),
                                    new TreeNode(20,
                                            new TreeNode(15),
                                            new TreeNode(7))),
                            true
                    ),
                    Arguments.of(
                            new TreeNode(1,
                                    new TreeNode(2,
                                            new TreeNode(3,
                                                    new TreeNode(4),
                                                    new TreeNode(4)),
                                            new TreeNode(3)),
                                    new TreeNode(2)),
                            false
                    ),
                    Arguments.of(
                            new TreeNode(),
                            true
                    ),
                    Arguments.of(
                            new TreeNode(1,
                                    new TreeNode(2,
                                            new TreeNode(3,
                                                    new TreeNode(4),
                                                    null),
                                            null),
                                    new TreeNode(2,
                                            null,
                                            new TreeNode(3,
                                                    null,
                                                    new TreeNode(4)))),
                            false
                    )
            );
        }
    }
}
