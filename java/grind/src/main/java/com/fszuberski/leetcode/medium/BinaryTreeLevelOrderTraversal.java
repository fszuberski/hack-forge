package com.fszuberski.leetcode.medium;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 102. Binary Tree Level Order Traversal
// https://leetcode.com/problems/binary-tree-level-order-traversal

public class BinaryTreeLevelOrderTraversal {

    List<List<Integer>> levelOrder(TreeNode root) {
        if (root == null) {
            return List.of();
        }

        Queue<List<TreeNode>> treeNodeQueue = new LinkedList<>();
        treeNodeQueue.add(List.of(root));

        return levelOrderHelper(treeNodeQueue, new ArrayList<>());
    }

    private List<List<Integer>> levelOrderHelper(Queue<List<TreeNode>> queue, List<List<Integer>> result) {
        if (queue.isEmpty()) {
            return result;
        }

        var currentLevelNodes = queue.poll();
        var nextLevelNodes = new ArrayList<TreeNode>();
        var currentLevelResult = new ArrayList<Integer>();
        for (var treeNode : currentLevelNodes) {
            currentLevelResult.add(treeNode.val);
            if (treeNode.left != null) {
                nextLevelNodes.add(treeNode.left);
            }
            if (treeNode.right != null) {
                nextLevelNodes.add(treeNode.right);
            }
        }
        if (!currentLevelResult.isEmpty()) {
            result.add(currentLevelResult);
        }

        if (!nextLevelNodes.isEmpty()) {
            queue.add(nextLevelNodes);
        }

        return levelOrderHelper(queue, result);
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

    static class BinaryTreeLevelOrderTraversalTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(TreeNode root, List<List<Integer>> expectedResult) {
            final var result = new BinaryTreeLevelOrderTraversal().levelOrder(root);
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
                            List.of(
                                    List.of(3),
                                    List.of(9, 20),
                                    List.of(15, 7))
                    ),
                    Arguments.of(
                            new TreeNode(1),
                            List.of(
                                    List.of(1)
                            )
                    ),
                    Arguments.of(
                            null,
                            List.of()
                    )
            );
        }
    }
}
