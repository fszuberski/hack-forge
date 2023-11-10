package ch2

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CurryingTest : DescribeSpec({

    describe("curry") {
        it("should sum two numbers") {
            curriedSum(1)(2) shouldBe 3
        }

        it("should sum multiple numbers curried") {
            val result = listOf(1, 2, 3, 4, 5).fold(0) { acc, i ->
                curriedSum(acc)(i)
            }
            result shouldBe 15
        }
    }

    describe("uncurry") {
        it("should sum two numbers") {
            uncurriedSum(1, 2) shouldBe 3
        }

        it("should sum multiple numbers") {
            val result = listOf(1, 2, 3, 4, 5).fold(0) { acc, i ->
                uncurriedSum(acc, i)
            }
            result shouldBe 15
        }
    }

    describe("sum") {
        it("should sum two numbers") {
            sum(5, 7) shouldBe 12
        }
    }
})