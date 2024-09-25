package com.fszuberski.snippets.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class Interfaces {


    public interface A {
        String name = "A";
        String a();

        default String aDefault() {
            return name;
        }

        default void print() {
            System.out.printf("My name is %s%n", name);
        }

    }

    public interface B {
        String name = "B";

        default void print() {
            System.out.printf("My name is %s%n", name);
        }
    }

    static class InterfacesTest {

        @Test
        @DisplayName("""
        a class must provide an implementation for a default method if implementing
        two or more interfaces that define a default method with the same signature
        """)
        public void implementingDefaultMethodsWithTheSameSignature() {

            class Implementor implements A, B {

                @Override
                public String a() {
                    return "A B";
                }

                @Override
                public void print() {
                    System.out.printf("My name is %s%n", a());
                }
            }

            var implementor = new Implementor();
            implementor.print();
        }
    }
}

