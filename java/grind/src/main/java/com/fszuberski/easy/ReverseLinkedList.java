package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 206. Reverse Linked List
// https://leetcode.com/problems/reverse-linked-list

public class ReverseLinkedList {

    ListNode reverseList(ListNode head) {
        if (head == null) {
            return null;
        }

        ListNode previous = null;
        ListNode current = head;
        ListNode next = head.next;

        while (current != null) {
            current.next = previous;
            previous = current;
            current = next;
            if (next != null) {
                next = next.next;
            }
        }

        return previous;
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


    static class RansomNoteTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(ListNode head, ListNode expectedResult) {
            final var result = new ReverseLinkedList().reverseList(head);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            ListNode.fromList(List.of(1, 2, 3, 4, 5)),
                            ListNode.fromList(List.of(5, 4, 3, 2, 1))
                    ),
                    Arguments.of(
                            ListNode.fromList(List.of(1, 2)),
                            ListNode.fromList(List.of(2, 1))
                    ),
                    Arguments.of(
                            ListNode.fromList(List.of()),
                            ListNode.fromList(List.of())
                    )
            );
        }
    }
}
