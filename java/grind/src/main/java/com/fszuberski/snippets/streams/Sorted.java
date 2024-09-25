package com.fszuberski.snippets.streams;

import com.fszuberski.snippets.Util.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.fszuberski.snippets.Util.peopleList;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Sorted {

    public List<Person> sortedPeople(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order cannot be null");
        }

        return switch (order) {
            case Order.ASC -> sortedPeople(Person::ageDifference);
            case Order.DESC -> sortedPeople((person1, person2) -> person2.ageDifference(person1));
        };
    }

    public List<Person> sortedPeopleFluentComparisons(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order cannot be null");
        }

        var compositeAscendingComparator = Stream
                .of(comparing(Person::age), comparing(Person::name))
                .reduce(Comparator::thenComparing)
                .get();

        return switch (order) {
            case Order.ASC -> sortedPeople(compositeAscendingComparator);
            case Order.DESC -> sortedPeople(compositeAscendingComparator.reversed());
        };
    }

    private List<Person> sortedPeople(Comparator<Person> peopleComparator) {
        return peopleList.stream()
                .sorted(peopleComparator)
                .toList();
    }

    public enum Order {
        ASC,
        DESC
    }

    public static class SortedTest {

        public static class SingleComparisons {

            private Sorted sorted;

            @BeforeEach
            public void beforeEach() {
                this.sorted = new Sorted();
            }

            @Test
            public void shouldReturnSortedPeopleAscendingOrder() {
                var result = sorted.sortedPeople(Order.ASC);

                assertEquals(4, result.size());
                assertEquals("Daniel", result.get(0).name());
                assertEquals("Emma", result.get(1).name());
                assertEquals("Cloe", result.get(2).name());
                assertEquals("Cezary", result.get(3).name());
            }

            @Test
            public void shouldReturnSortedPeopleDescendingOrder() {
                var result = sorted.sortedPeople(Order.DESC);

                assertEquals(4, result.size());
                assertEquals("Cezary", result.get(0).name());
                assertEquals("Cloe", result.get(1).name());
                assertEquals("Emma", result.get(2).name());
                assertEquals("Daniel", result.get(3).name());
            }

            @Test
            public void shouldThrowExceptionGivenNullOrder() {
                assertThrows(IllegalArgumentException.class,
                        () -> sorted.sortedPeople((Order) null));
            }
        }

        public static class MultipleAndFluentComparisons {

            private Sorted sorted;

            @BeforeEach
            public void beforeEach() {
                this.sorted = new Sorted();
            }

            @Test
            public void shouldReturnSortedPeopleAscendingOrder() {
                var result = sorted.sortedPeopleFluentComparisons(Order.ASC);

                assertEquals(4, result.size());
                assertEquals("Daniel", result.get(0).name());
                assertEquals("Emma", result.get(1).name());
                assertEquals("Cloe", result.get(2).name());
                assertEquals("Cezary", result.get(3).name());
            }

            @Test
            public void shouldReturnSortedPeopleDescendingOrder() {
                var result = sorted.sortedPeopleFluentComparisons(Order.DESC);

                assertEquals(4, result.size());
                assertEquals("Cezary", result.get(0).name());
                assertEquals("Cloe", result.get(1).name());
                assertEquals("Emma", result.get(2).name());
                assertEquals("Daniel", result.get(3).name());
            }

            @Test
            public void shouldThrowExceptionGivenNullOrder() {
                assertThrows(IllegalArgumentException.class,
                        () -> sorted.sortedPeople((Order) null));
            }
        }
    }
}
