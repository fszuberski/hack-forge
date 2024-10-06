package com.fszuberski.leetcode.easy;

import java.util.Stack;

// 232. Implement Queue using Stacks
// https://leetcode.com/problems/implement-queue-using-stacks

public class ImplementQueueUsingStacks {

    class MyQueue {

        private final Stack<Integer> insertStack = new Stack<>();
        private final Stack<Integer> retrieveStack = new Stack<>();
        private Mode mode = Mode.INSERT;

        public void push(int x) {
            toInsertMode();
            insertStack.push(x);
        }

        public int pop() {
            toRetrieveMode();
            return retrieveStack.pop();
        }

        public int peek() {
            toRetrieveMode();
            return retrieveStack.peek();
        }

        public boolean empty() {
            return insertStack.isEmpty() && retrieveStack.isEmpty();
        }

        private void toInsertMode() {
            if (mode == Mode.INSERT) {
                return;
            }

            while (!retrieveStack.isEmpty()) {
                insertStack.push(retrieveStack.pop());
            }

            mode = Mode.INSERT;
        }

        private void toRetrieveMode() {
            if (mode == Mode.RETRIEVE) {
                return;
            }

            while (!insertStack.isEmpty()) {
                retrieveStack.push(insertStack.pop());
            }

            mode = Mode.RETRIEVE;
        }

        private enum Mode {
            INSERT, RETRIEVE
        }
    }

}
