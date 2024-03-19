package com.fszuberski.easy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 21. Merge Two Sorted Lists
// https://leetcode.com/problems/merge-two-sorted-lists

public class MergeTwoSortedLists {

    ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        ListNode head = null;
        ListNode current = null;

        while (list1 != null || list2 != null) {
            if (list1 != null && list2 != null) {
                if (list1.val <= list2.val) {
                    if (head == null) {
                        current = list1;
                        head = current;
                    } else {
                        current.next = list1;
                        current = current.next;
                    }
                    list1 = list1.next;

                } else {
                    if (head == null) {
                        current = list2;
                        head = current;
                    } else {
                        current.next = list2;
                        current = current.next;
                    }
                    list2 = list2.next;
                }

            } else if (list1 != null) {
                if (head == null) {
                    current = list1;
                    head = current;
                } else {
                    current.next = list1;
                    current = current.next;
                }
                list1 = list1.next;

            } else {
                if (head == null) {
                    current = list2;
                    head = current;
                } else {
                    current.next = list2;
                    current = current.next;
                }
                list2 = list2.next;
            }
        }


        return head;
    }

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
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

    static class LinkedListCycleTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(ListNode list1, ListNode list2, ListNode expectedResult) {
            final var result = new MergeTwoSortedLists().mergeTwoLists(list1, list2);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of(
                            ListNode.fromList(List.of(1, 2, 4)),
                            ListNode.fromList(List.of(1, 3, 4)),
                            ListNode.fromList(List.of(1, 1, 2, 3, 4, 4))
                    ),
                    Arguments.of(
                            ListNode.fromList(List.of()),
                            ListNode.fromList(List.of()),
                            ListNode.fromList(List.of())
                    ),
                    Arguments.of(
                            ListNode.fromList(List.of(0)),
                            ListNode.fromList(List.of()),
                            ListNode.fromList(List.of(0))
                    ),
                    Arguments.of(
                            ListNode.fromList(List.of()),
                            ListNode.fromList(List.of(0)),
                            ListNode.fromList(List.of(0))
                    )
            );
        }
    }
}
