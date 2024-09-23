package com.fszuberski.snippets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Super {

    public static class Vehicle {
        private final String make;

        public Vehicle(String make) {
            this.make = make;
        }

        private void printMake() {
            System.out.printf("Make is: %s%n", make);
        }
    }

    public static class Truck extends Vehicle {

        public Truck(String make) {
            super(make);
        }

        public String getMake() {
            super.printMake();
            return super.make;
        }
    }

    static class SuperTest {

        @Test
        @DisplayName("sublass can access private members of parent class via super keyword")
        public void subclassCanAccessPrivateMembersOfParentClassViaSuperKeyword() {
            var expectedMake = "Mercedes";
            var truck = new Truck(expectedMake);

            assertEquals(expectedMake, truck.getMake());
        }

    }
}
