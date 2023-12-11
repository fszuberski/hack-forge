import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class CoroutineContextOverview {

    @Test
    fun `adding coroutine contexts`() {
        val ctx1: CoroutineContext = CoroutineName("Name1")
        println(ctx1[CoroutineName]?.name) // Name1
        println(ctx1[Job]?.isActive) // null

        val ctx2: CoroutineContext = Job()
        println(ctx2[CoroutineName]?.name) // null
        println(ctx2[Job]?.isActive) // true, because "Active" is the default state of a job created this way

        val ctx3 = ctx1 + ctx2
        println(ctx3[CoroutineName]?.name) // Name1
        println(ctx3[Job]?.isActive) // true
    }

    @Test
    fun `subracting elements from coroutine context`() {
        val ctx: CoroutineContext = CoroutineName("Name") + Job()
        println(ctx[CoroutineName]?.name) // Name
        println(ctx[Job]?.isActive) // true

        val ctx2 = ctx.minusKey(Job)
        println(ctx2[CoroutineName]?.name) // Name
        println(ctx2[Job]?.isActive) // null
    }

    @Test
    fun `folding context`() {
        val ctx = CoroutineName("Name1") + Job()
        ctx.fold("") { acc, element -> "$acc$element " }
            .also(::println)
        // CoroutineName(Name1) JobImpl{Active}@691a7f8f

        val empty = emptyList<CoroutineContext>()
        ctx.fold(empty) { acc, element -> acc + element }
            .joinToString()
            .also(::println)
        // CoroutineName(Name1), JobImpl{Active}@dbab622e
    }

    @Test
    fun `passing context from parent to child`() = runBlocking(CoroutineName("main")) {
        log("Started") // [main] Started
        val v1 = async {
            delay(500)
            log("Running async") // [main] Running async
            42
        }
        launch {
            delay(1000)
            log("Running launch") // [main] Running launch
        }
        log("The answer is ${v1.await()}") // [main] The answer is 42
        // [main] Started
        // [main] Running async
        // [main] The answer is 42
        // [main] Running launch
    }

    @Test
    fun `overriding context in child coroutine`() = runBlocking(CoroutineName("main")) {
        log("Started") // [main] Started
        val v1 = async(CoroutineName("c1")) {
            delay(500)
            log("Running async") // [c1] Running async
            42
        }
        launch(CoroutineName("c2"))
        {
            delay(1000)
            log("Running launch") // [c2] Running launch
        }
        log("The answer is ${v1.await()}") // [main] The answer is 42
        // [main] Started
        // [c1] Running async
        // [main] The answer is 42
        // [c2] Running launch

        // Note:
        // - each child receives a copy of the context, original context is not mutated
    }

    @Test
    fun `accessing context from a suspending function`() {
        suspend fun printName() {
            println(coroutineContext[CoroutineName]?.name)
        }

        suspend fun suspendingFunction() {
            withContext(CoroutineName("Outer")) {
                printName() // Outer
                launch(CoroutineName("Inner")) {
                    printName() // Inner
                }
                delay(10)
                printName() // Outer
            }
        }

        runBlocking { suspendingFunction() }
        // Outer
        // Inner
        // Outer
    }


    class CounterContext(
        private val name: String
    ) : CoroutineContext.Element {
        override val key: CoroutineContext.Key<*> = Key
        private var nextNumber = 0
        fun printNext() {
            println("$name: $nextNumber")
            nextNumber++
        }

        companion object Key : CoroutineContext.Key<CounterContext>
    }

    @Test
    fun `creating a custom context`() {
        suspend fun printNext() {
            coroutineContext[CounterContext]?.printNext()
        }

        suspend fun suspendingFunction() =
            withContext(CounterContext("Outer")) {
                printNext() // Outer: 0
                launch {
                    printNext() // Outer: 1
                    launch {
                        printNext() // Outer: 2
                    }
                    launch(CounterContext("Inner")) {
                        printNext() // Inner: 0
                        printNext() // Inner: 1
                        launch {
                            printNext() // Inner: 2
                        }
                    }
                }
                printNext() // Outer: 3
            }

        runBlocking { suspendingFunction() }

        // Outer: 0
        // Outer: 1
        // Outer: 2
        // Outer: 3
        // Inner: 0
        // Inner: 1
        // Inner: 2
    }
}

private fun CoroutineScope.log(msg: String) {
    val name = coroutineContext[CoroutineName]?.name
    println("[$name] $msg")
}