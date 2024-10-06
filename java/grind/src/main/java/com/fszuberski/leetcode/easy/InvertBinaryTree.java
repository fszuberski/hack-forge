package com.fszuberski.leetcode.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 226. Invert Binary Tree
// https://leetcode.com/problems/invert-binary-tree/

class InvertBinaryTree {

    public TreeNode invertTree(TreeNode root) {
        if (root == null) {
            return root;
        }

        if (root.left == null && root.right == null) {
            return root;
        }

        if (root.left != null) {
            invertTree(root.left);
        }

        if (root.right != null) {
            invertTree(root.right);
        }

        var tmp = root.left;
        root.left = root.right;
        root.right = tmp;

        return root;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TreeNode treeNode = (TreeNode) o;
            return val == treeNode.val && Objects.equals(left, treeNode.left) && Objects.equals(right, treeNode.right);
        }

        @Override
        public int hashCode() {
            return Objects.hash(val, left, right);
        }
    }

    static class InvertBinaryTreeTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(TreeNode root, TreeNode expectedResult) {
            final var result = new InvertBinaryTree().invertTree(root);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            new TreeNode(4,
                                    new TreeNode(2,
                                            new TreeNode(6),
                                            new TreeNode(9)),
                                    new TreeNode(7,
                                            new TreeNode(1),
                                            new TreeNode(3))),
                            new TreeNode(4,
                                    new TreeNode(7,
                                            new TreeNode(3),
                                            new TreeNode(1)),
                                    new TreeNode(2,
                                            new TreeNode(9),
                                            new TreeNode(6)))
                    ),
                    Arguments.of(
                            new TreeNode(2,
                                    new TreeNode(1),
                                    new TreeNode(3)),
                            new TreeNode(2,
                                    new TreeNode(3),
                                    new TreeNode(1))
                    ),
                    Arguments.of(
                            new TreeNode(),
                            new TreeNode()
                    )
            );
        }
    }
}
