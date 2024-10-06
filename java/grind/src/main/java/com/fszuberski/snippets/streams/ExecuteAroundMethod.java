package com.fszuberski.snippets.streams;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class ExecuteAroundMethod {

    public void execute() {
        ExpensiveResource.use(ExpensiveResource::compute);
    }

    public static class ExpensiveResource implements AutoCloseable {
        private ExpensiveResource() {
        }

        private void compute() {
            System.out.println("Computing...");
        }

        public static void use(Consumer<ExpensiveResource> block) {
            try (var resource = new ExpensiveResource()) {
                block.accept(resource);
            }
        }

        @Override
        public void close() {
            System.out.printf("Closing %s...%n", this.getClass().getSimpleName());
        }
    }

    public static class ExecuteAroundMethodTest {

        @Test
        public void executeTest() {
            new ExecuteAroundMethod().execute();
        }
    }
}
