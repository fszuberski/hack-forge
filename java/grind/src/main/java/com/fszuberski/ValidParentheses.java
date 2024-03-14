package com.fszuberski;

// 20. Valid Parentheses
// https://leetcode.com/problems/valid-parentheses

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Stack;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidParentheses {

    public boolean isValid(String string) {
        var characterStack = new Stack<Character>();
        var isValid = stringToCharStream(string)
                .allMatch(character ->
                        switch (character) {
                            case '(', '{', '[' -> {
                                characterStack.push(character);
                                yield true;
                            }
                            default -> isMatchingRightParenthesis(characterStack, character);
                        }
                );

        return characterStack.isEmpty() && isValid;
    }

    private boolean isMatchingRightParenthesis(Stack<Character> characterStack, Character rightParenthesis) {
        if (characterStack.isEmpty()) {
            return false;
        }

        final var popped = characterStack.pop();
        return switch (rightParenthesis) {
            case ')' -> popped == '(';
            case '}' -> popped == '{';
            case ']' -> popped == '[';

            default -> false;
        };
    }

    private Stream<Character> stringToCharStream(String string) {
        return string.chars()
                .mapToObj(character -> (char) character);
    }

    static class ValidParenthesesTest {

        @ParameterizedTest
        @MethodSource("testCases")
        void test(String input, boolean expectedResult) {
            final var result = new ValidParentheses().isValid(input);
            assertEquals(expectedResult, result);
        }

        private static Stream<Arguments> testCases() {
            return Stream.of(
                    Arguments.of("()", true),
                    Arguments.of("()[]{}", true),
                    Arguments.of("(]", false)
            );
        }
    }
}


