/*
 Exercise 2.1 / page 20
 Write a recursive function to get the nth Fibonacci number (https://www.britannica .com/science/Fibonacci-number).
 The first two Fibonacci numbers are 0 and 1. The nth number is always the sum of the previous two—the sequence begins
 0, 1, 1, 2, 3, 5, 8, 13, 21. Your definition should use a local tail-recursive function.
 tag: tailrec
*/

fun fibonacci(i: Int) = fibonacciTailRec(i)

fun fibonacciTailRec(i: Int): Int {
    tailrec fun loop(n: Int, a: Int = 0, b: Int = 1): Int =
        when(n) {
            0 -> a
            1 -> b
            else -> loop(n - 1, b, a + b)
        }

    return loop(i)
}

fun fibonacciClassic(i: Int): Int {
    if (i == 0) {
        return 0
    }
    if (i == 1) {
        return 1
    }

    return fibonacciClassic(i - 1) + fibonacciClassic(i - 2)
}
