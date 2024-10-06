package com.fszuberski.demica;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Interview {
    // Case 1
//    public static void main(String[] args) {
//        int[] spy = {0};
//        Optional<String> opt = Optional.of("this");
//        Optional<String> before = opt.flatMap(s -> {
//            spy[0] = spy[0] + 1;
//            return Optional.of(s + " and that");
//        });
//        System.out.printf("Case 1 before - spy: %s%n", spy[0]);
//        String result = before.orElse("nothing");
//        System.out.printf("Case 1 after - result: %s, spy: %s%n", result, spy[0]);
//    }

//    // Case 2
//    public static void main(String[] args) {
//        int[] spy = {0};
//        Optional<String> opt = Optional.empty();
//        Optional<String> before = opt.flatMap(s -> {
//            spy[0] = spy[0] + 1;
//            return Optional.of(s + " and that");
//        });
//        System.out.printf("Case 2 before - spy: %s%n", spy[0]);
//        String result = before.orElse("nothing");
//        System.out.printf("Case 2 after - result: %s, spy: %s%n", result, spy[0]);
//    }
//
//    // Case 3
//    public static void main(String[] args) {
//        int[] spy = {0};
//        Optional<String> opt = Optional.of("this");
//        Optional<String> before = opt.flatMap(s -> {
//            spy[0] = spy[0] + 1;
//            return Optional.empty();
//        });
//        System.out.printf("Case 3 before - spy: %s%n", spy[0]);
//        String result = before.orElse("nothing");
//        System.out.printf("Case 3 after - result: %s, spy: %s%n", result, spy[0]);
//    }
//
//    // Case 4
//    public static void main(String[] args) {
//        int[] spy = {0};
//        Optional<String> opt = Optional.of("this");
//        Optional<String> before = opt
//                .flatMap(s -> s.startsWith("t") ? Optional.empty() : Optional.of(s))
//                .flatMap(s -> {
//                    spy[0] = spy[0] + 1;
//                    return Optional.of(s + " and that");
//                });
//        System.out.printf("Case 4 before - spy: %s%n", spy[0]);
//        String result = before.orElse("nothing");
//        System.out.printf("Case 4 after - result: %s, spy: %s%n", result, spy[0]);
//    }
//
//    // Case 5
//    public static void main(String[] args) {
//        int[] spy = {0};
//        Optional<String> opt = Optional.of("this");
//        Optional<String> before = opt
//                .flatMap(s -> s.startsWith("t") ? Optional.of(s) : Optional.empty())
//                .flatMap(s -> {
//                    spy[0] = spy[0] + 1;
//                    return Optional.of(s + " and that");
//                });
//        System.out.printf("Case 5 before - spy: %s%n", spy[0]);
//        String result = before.orElse("nothing");
//        System.out.printf("Case 5 after - result: %s, spy: %s%n", result, spy[0]);
//    }
//
//    // Case 6
//    public static void main(String[] args) {
//        int[] spy = {0};
//        Stream<String> stream = Stream.of("Gasoline", "Diesel");
//        Stream<String> before = stream.flatMap(s -> {
//            spy[0] = spy[0] + 1;
//            return Stream.of(s + " is fuel");
//        });
//        System.out.printf("Case 6 before - spy: %s%n", spy[0]);
//        List<String> result = before.collect(Collectors.toList());
//        System.out.printf("Case 6 after - result: %s, spy: %s%n", result, spy[0]);
//    }
//
//    // Case 7
//    public static void main(String[] args) {
//        int[] spy = {0};
//        Stream<String> stream = Stream.of("Gasoline", "Diesel");
//        Stream<String> before = stream.flatMap(s -> {
//            spy[0] = spy[0] + 1;
//            return Stream.of(s, "is fuel");
//        });
//        System.out.printf("Case 7 before - spy: %s%n", spy[0]);
//        List<String> result = before.collect(Collectors.toList());
//        System.out.printf("Case 7 after - result: %s, spy: %s%n", result, spy[0]);
//    }
//
//    // Case 8
//    public static void main(String[] args) {
//        int[] spy = {0};
//        Stream<String> stream = Stream.empty();
//        Stream<String> before = stream.flatMap(s -> {
//            spy[0] = spy[0] + 1;
//            return Stream.of(s, "is fuel");
//        });
//        System.out.printf("Case 8 before - spy: %s%n", spy[0]);
//        List<String> result = before.collect(Collectors.toList());
//        System.out.printf("Case 8 after - result: %s, spy: %s%n", result, spy[0]);
//    }
//
//    // Case 9
    public static void main(String[] args) {
        int[] spy = {0};
        Stream<String> stream = Stream.of("pineapple", "strawberry", "mango");
        Stream<String> before = stream
                .flatMap(s -> s.startsWith("s") ? Stream.empty() : Stream.of(s))
                // pineapple, mango
                .flatMap(s -> {
                    spy[0] = spy[0] + 1;
                    return Stream.of(s, "is a fruit");
                });
        System.out.printf("Case 9 before - spy: %s%n", spy[0]);
        List<String> result = before.collect(Collectors.toList());
        System.out.printf("Case 9 after - result: %s, spy: %s%n", result, spy[0]);
    }
}
