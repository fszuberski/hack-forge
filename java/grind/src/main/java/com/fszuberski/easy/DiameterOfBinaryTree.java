package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 543. Diameter of Binary Tree
// https://leetcode.com/problems/diameter-of-binary-tree

public class DiameterOfBinaryTree {

    int diameterOfBinaryTree(TreeNode root) {
        var context = new Context(0);
        diameterOfBinaryTreeHelper(context, root);

        return context.maxDiameter;
    }

    private int diameterOfBinaryTreeHelper(Context context, TreeNode root) {
        if (root == null) {
            return 0;
        }

        var leftHeight = diameterOfBinaryTreeHelper(context, root.left);
        var rightHeight = diameterOfBinaryTreeHelper(context, root.right);
        context.maxDiameter = Math.max(context.maxDiameter, leftHeight + rightHeight);

        return 1 + Math.max(leftHeight, rightHeight);
    }

    private static class Context {
        int maxDiameter;

        public Context(int maxDiameter) {
            this.maxDiameter = maxDiameter;
        }
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

    static class DiameterOfBinaryTreeTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(TreeNode root, int expectedResult) {
            final var result = new DiameterOfBinaryTree().diameterOfBinaryTree(root);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            new TreeNode(1,
                                    new TreeNode(2,
                                            new TreeNode(4),
                                            new TreeNode(5)),
                                    new TreeNode(3)),
                            3
                    ),
                    Arguments.of(
                            new TreeNode(1,
                                    new TreeNode(2),
                                    null),
                            1
                    )
            );
        }
    }
}
