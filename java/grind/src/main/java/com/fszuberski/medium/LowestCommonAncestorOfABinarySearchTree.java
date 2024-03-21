package com.fszuberski.medium;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 235. Lowest Common Ancestor of a Binary Search Tree
// https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-search-tree

public class LowestCommonAncestorOfABinarySearchTree {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (root.val > p.val && root.val > q.val) {
            return lowestCommonAncestor(root.left, p, q);
        }

        if (root.val < p.val && root.val < q.val) {
            return lowestCommonAncestor(root.right, p, q);
        }

        return root;
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

    static class LowestCommonAncestorOfABinarySearchTreeTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(TreeNode root, TreeNode p, TreeNode q, TreeNode expectedResult) {
            final var result = new LowestCommonAncestorOfABinarySearchTree().lowestCommonAncestor(root, p, q);
            assertEquals(expectedResult.val, result.val);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            new TreeNode(6,
                                    new TreeNode(2,
                                            new TreeNode(0),
                                            new TreeNode(4,
                                                    new TreeNode(3),
                                                    new TreeNode(5))),
                                    new TreeNode(8,
                                            new TreeNode(7),
                                            new TreeNode(9))
                            ),
                            new TreeNode(2),
                            new TreeNode(8),
                            new TreeNode(6)
                    ),
                    Arguments.of(
                            new TreeNode(6,
                                    new TreeNode(2,
                                            new TreeNode(0),
                                            new TreeNode(4,
                                                    new TreeNode(3),
                                                    new TreeNode(5))),
                                    new TreeNode(8,
                                            new TreeNode(7),
                                            new TreeNode(9))
                            ),
                            new TreeNode(2),
                            new TreeNode(4),
                            new TreeNode(2)
                    ),
                    Arguments.of(
                            new TreeNode(2,
                                    new TreeNode(1),
                                    null),
                            new TreeNode(2),
                            new TreeNode(1),
                            new TreeNode(2)
                    )
            );
        }
    }
}