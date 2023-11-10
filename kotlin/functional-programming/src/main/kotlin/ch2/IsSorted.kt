package ch2

/*
Exercise 2.2 / page 25
Implement isSorted, which checks whether a singly linked list List<A> is sorted according to a given comparison
function. The function is preceded by two extension properties that add head and tail to any List value. The head
property returns the first element of the list, while tail returns all subsequent elements as another List<A>.
tags: tailrec, extension functions
*/

val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun loop(l: List<A>, acc: Boolean = true): Boolean =
        if (!acc) false
        else when (l.size) {
            0 -> true
            1 -> true
            else -> loop(l.tail, order(l.head, l.tail.head))
        }

    return loop(aa)
}

fun asc(first: Int, second: Int): Boolean = first <= second
fun desc(first: Int, second: Int): Boolean = first >= second

