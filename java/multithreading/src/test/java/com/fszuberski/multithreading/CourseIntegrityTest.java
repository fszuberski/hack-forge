package com.fszuberski.multithreading;

import com.fszuberski.multithreading.support.ExerciseContracts;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CourseIntegrityTest {
    private static final Path ROOT = Path.of(System.getProperty("course.projectDir", "."));

    static Stream<Path> topics() throws Exception {
        try (var files = Files.list(ROOT.resolve("docs/topics"))) {
            var topics = files.filter(path -> path.toString().endsWith(".md")).sorted().toList();
            assertEquals(16, topics.size());
            return topics.stream();
        }
    }

    static Stream<Class<?>> contracts() {
        return ExerciseContracts.registeredClasses().stream();
    }

    @Nested
    class Curriculum {
        @ParameterizedTest
        @MethodSource("com.fszuberski.multithreading.CourseIntegrityTest#topics")
        void shouldProvideTheoryExercisesHintsAndInterviewPromptsGivenTopic(Path topic) throws Exception {
            String text = Files.readString(topic);
            for (String heading : List.of("## Theory", "## Exercise A", "## Exercise B",
                    "## Hints", "## Interview")) {
                assertTrue(text.contains(heading), topic + " is missing " + heading);
            }
        }
    }

    @Nested
    class ExerciseBoundary {
        @Test
        void shouldRegisterExactlyThirtyTwoContractsGivenDiscoveredExerciseTests() throws Exception {
            var registered = ExerciseContracts.registeredClasses();
            assertEquals(32, registered.size());
            assertEquals(32, new HashSet<>(registered).size(), "No duplicate contracts");
            Set<Class<?>> discovered = ExerciseContracts.sourceTestClasses(ROOT.resolve("src/test/java"))
                    .stream().filter(type -> ExerciseContracts.testCases(type).stream()
                            .anyMatch(test -> test.tags().contains("exercise")))
                    .collect(Collectors.toSet());
            assertEquals(new HashSet<>(registered), discovered, "No missing or unregistered exercises");
        }

        @ParameterizedTest
        @MethodSource("com.fszuberski.multithreading.CourseIntegrityTest#contracts")
        void shouldRequireOptInAndSafetyDeadlinesGivenExerciseContract(Class<?> contract) {
            var cases = ExerciseContracts.testCases(contract);
            assertFalse(cases.isEmpty(), contract + " must have executable acceptance cases");
            for (var test : cases) {
                assertTrue(test.tags().contains("exercise"), test.method() + " must be opt-in");
                assertNotNull(test.timeout(), test.method() + " needs a safety deadline");
                assertTrue(test.timeout().value() > 0, test.method() + " needs a positive deadline");
                assertTrue(test.method().getName().matches("should.+Given.+"),
                        test.method() + " needs a behavior-oriented name");
            }
        }
    }
}
