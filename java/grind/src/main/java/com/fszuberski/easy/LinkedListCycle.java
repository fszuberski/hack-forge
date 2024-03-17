package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 141. Linked List Cycle
// https://leetcode.com/problems/linked-list-cycle

class LinkedListCycle {

    // O(1) memory
    public boolean hasCycle(ListNode head) {
        if (head == null || head.next == null) {
            return false;
        }

        var slow = head;
        var fast = head.next.next;

        while (slow != null && fast != null) {
            if (slow == fast) {
                return true;
            }

            slow = slow.next;
            if (fast.next != null) {
                fast = fast.next.next;
            } else {
                return false;
            }
        }

        return false;
    }

    // O(n) memory
    public boolean hasCycle_lesserPerformance(ListNode head) {
        var visited = new HashSet<ListNode>();

        while (head != null) {
            if (visited.contains(head)) {
                return true;
            }

            visited.add(head);
            head = head.next;
        }

        return false;
    }

    static class ListNode {
        Integer val;
        ListNode next;

        public ListNode(Integer val, ListNode next) {
            this.val = val;
            this.next = next;
        }

        public static ListNode fromListAndCyclePosition(List<Integer> values, int position) {
            int idx = values.size() - 1;
            ListNode currentNode = null;
            ListNode last = null;
            ListNode atPosition = null;
            while (idx >= 0) {
                currentNode = new ListNode(values.get(idx), currentNode);

                if (last == null) {
                    last = currentNode;
                }

                if (idx == position) {
                    atPosition = currentNode;
                }

                --idx;
            }

            if (last != null) {
                last.next = atPosition;
            }

            return currentNode;
        }
    }

    static class LinkedListCycleTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(ListNode head, boolean expectedResult) {
            final var result = new LinkedListCycle().hasCycle(head);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    // Input: head = [3,2,0,-4], pos = 1
                    // Output: true
                    Arguments.of(ListNode.fromListAndCyclePosition(List.of(3, 2, 0, -4), 1), true),
                    //  Input: head = [1,2], pos = 0
                    // Output: true
                    Arguments.of(ListNode.fromListAndCyclePosition(List.of(1, 2), 0), true),
                    // Input: head = [1], pos = -1
                    // Output: false
                    Arguments.of(ListNode.fromListAndCyclePosition(List.of(1), -1), false),
                    // Input: head = [3,2,0,-4, 3, 7, 5], pos = -1
                    // Output: false
                    Arguments.of(ListNode.fromListAndCyclePosition(List.of(3, 2, 0, -4, 3, 7, 5), -1), false)
            );
        }
    }
}
