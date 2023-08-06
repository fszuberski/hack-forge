import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IsSortedTest : DescribeSpec({

    describe("isSorted") {
        it("should return true if a list contains no element") {
            isSorted(listOf(), ::asc) shouldBe true
        }
        it("should return true if a list contains a single element") {
            isSorted(listOf(5), ::asc) shouldBe true
        }
        it("should return true if a list is not sorted") {
            isSorted(listOf(1, 3, 4, 6, 7, 8), ::asc) shouldBe true
        }
        it("should return false if a list is not sorted") {
            isSorted(listOf(1, 3, 4, 2, 1), ::asc) shouldBe false
        }
    }

    describe("asc") {
        it("should return true if parameters are in ascending order") {
            asc(1, 2) shouldBe true
        }
        it("should return true if parameters are equal") {
            asc(2, 2) shouldBe true
        }
        it("should return false if parameters are in descending order") {
            asc(2, 1) shouldBe false
        }
    }

    describe("desc") {
        it("should return true if parameters are in descending order") {
            desc(2, 1) shouldBe true
        }
        it("should return true if parameters are equal") {
            desc(2, 2) shouldBe true
        }
        it("should return false if parameters are in ascending order") {
            desc(1, 2) shouldBe false
        }
    }
})