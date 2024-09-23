package com.fszuberski.snippets;

import org.junit.jupiter.api.Test;

public class Initialization {

    public static class Car {
        static String sound;

        static {
            System.out.printf("[%s]: invoked static initialization block %n", Car.class.getSimpleName());
            sound = "beep beep";
            System.out.printf("[%s]: static initialization block finished %n", Car.class.getSimpleName());
        }
    }

    static class InitializationTest {

        @Test
        public void demo() {
            System.out.printf("static Car.sound field value: %s%n", Car.sound);

            System.out.println("creating new car instance");
            var car = new Car();
            System.out.printf("new car instance created: %s%n", car);


            System.out.println("creating a second car instance");
            var car2 = new Car();
            System.out.printf("second car instance created: %s%n", car2);
        }
    }
}

