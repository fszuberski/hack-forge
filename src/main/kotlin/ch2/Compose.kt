package ch2

/*
Exercise 2.5 / page 29
Implement the HOF that composes two functions.
 */
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a -> f(g(a)) }