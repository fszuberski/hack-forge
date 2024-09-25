package com.fszuberski.snippets;

import java.util.List;

public class Util {

    public static final List<Person> peopleList = List.of(
            new Person("Daniel", 17),
            new Person("Cezary", 78),
            new Person("Cloe", 43),
            new Person("Emma", 21)
    );

    public record Person(String name, int age) {
        public Person {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name cannot be empty");
            }
            if (age < 0) {
                throw new IllegalArgumentException("age cannot be less than 0");
            }
        }

        public int ageDifference(Person other) {
            if (other == null) {
                throw new IllegalArgumentException("cannot compare age difference to null");
            }

            return this.age - other.age;
        }
    }
}
