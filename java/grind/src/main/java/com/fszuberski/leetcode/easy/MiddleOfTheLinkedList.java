package com.fszuberski.leetcode.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 876. Middle of the Linked List
// https://leetcode.com/problems/middle-of-the-linked-list

public class MiddleOfTheLinkedList {

    ListNode middleNode(ListNode head) {
        if (head == null) {
            return null;
        }

        var slow = head;
        var fast = head.next;

        while (fast != null) {
            slow = slow.next;
            fast = fast.next;
            if (fast != null) {
                fast = fast.next;
            }
        }

        return slow;
    }

    static class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }

        public static ListNode fromList(List<Integer> list) {
            ListNode head = null;
            ListNode current = null;
            for (Integer val : list) {
                if (head == null) {
                    current = new ListNode(val);
                    head = current;
                } else {
                    current.next = new ListNode(val);
                    current = current.next;
                }
            }

            return head;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ListNode listNode = (ListNode) o;
            return val == listNode.val && Objects.equals(next, listNode.next);
        }

        @Override
        public int hashCode() {
            return Objects.hash(val, next);
        }
    }


    static class MiddleOfLinkedListTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(ListNode head, ListNode expectedResult) {
            final var result = new MiddleOfTheLinkedList().middleNode(head);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            ListNode.fromList(List.of(1, 2, 3, 4, 5)),
                            ListNode.fromList(List.of(3, 4, 5))
                    ),
                    Arguments.of(
                            ListNode.fromList(List.of(1, 2, 3, 4, 5, 6)),
                            ListNode.fromList(List.of(4, 5, 6))
                    ),
                    Arguments.of(
                            ListNode.fromList(List.of()),
                            ListNode.fromList(List.of())
                    )
            );
        }
    }
}
