package com.fszuberski.leetcode.medium;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 98. Validate Binary Search Tree
// https://leetcode.com/problems/validate-binary-search-tree

public class ValidateBinarySearchTree {

    // 0 ms Beats 100.00% of users with Java
    public boolean isValidBST(TreeNode root) {
        return isValidBSTHelper(root.left, null, root.val) && isValidBSTHelper(root.right, root.val, null);
    }

    public boolean isValidBSTHelper(TreeNode node, Integer minVal, Integer maxVal) {
        if (node == null) {
            return true;
        }

        if ((minVal != null && node.val <= minVal) || (maxVal != null && node.val >= maxVal)) {
            return false;
        }

        return isValidBSTHelper(node.left, minVal, node.val) && isValidBSTHelper(node.right, node.val, maxVal);
    }

    public static class TreeNode {
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

    static class ValidateBinarySearchTreeTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(TreeNode root, boolean expectedResult) {
            final var result = new ValidateBinarySearchTree().isValidBST(root);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            new TreeNode(2,
                                    new TreeNode(1),
                                    new TreeNode(3)),
                            true
                    ),
                    Arguments.of(
                            new TreeNode(5,
                                    new TreeNode(1),
                                    new TreeNode(4,
                                            new TreeNode(3),
                                            new TreeNode(6))),
                            false
                    ),
                    Arguments.of(
                            new TreeNode(2,
                                    new TreeNode(2),
                                    new TreeNode(2)),
                            false
                    ),
                    Arguments.of(
                            new TreeNode(Integer.MIN_VALUE,
                                    null,
                                    new TreeNode(Integer.MAX_VALUE)),
                            true
                    )
            );
        }
    }
}
