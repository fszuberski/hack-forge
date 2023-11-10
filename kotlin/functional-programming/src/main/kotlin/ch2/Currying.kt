package ch2

/*
Exercise 2.3 / page 29
Let’s look at another example, currying, which converts a function f of two arguments into a function with one argument
that partially applies f.
(Currying is named after the mathematician Haskell Curry, who discovered the principle.
*/
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a -> { b -> f(a, b) } }

/*
Exercise 2.4 / page 29
Implement uncurry, which reverses the transformation of curry.
Note that since -> associates to the right, (A)->((B)->C) can be written as (A)->(B)->C.
*/
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a, b -> f(a)(b) }

val sum: (Int, Int) -> Int = { a, b -> a + b}
val curriedSum = curry(sum)
val uncurriedSum = uncurry(curriedSum)
