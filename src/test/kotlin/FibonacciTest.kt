import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FibonacciTest : StringSpec({
    "fib -1 should be 0" {
        fibonacci(0) shouldBe 0
    }
    "fib 0 should be 0" {
        fibonacci(0) shouldBe 0
    }
    "fib 1 should be 1" {
        fibonacci(1) shouldBe 1
    }
    "fib 2 should be 1" {
        fibonacci(2) shouldBe 1
    }
    "fib 3 should be 2" {
        fibonacci(3) shouldBe 2
    }
    "fib 7 should be 13" {
        fibonacci(7) shouldBe 13
    }
    "fib 13 should be 233" {
        fibonacci(13) shouldBe 233
    }
    "fib 20 should be 233" {
        fibonacci(13) shouldBe 233
    }
})