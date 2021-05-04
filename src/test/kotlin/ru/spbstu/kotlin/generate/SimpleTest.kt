package ru.spbstu.kotlin.generate

import org.junit.Test
import ru.spbstu.kotlin.generate.combinators.*
import ru.spbstu.kotlin.generate.util.supertypes
import ru.spbstu.kotlin.generate.util.typeOf
import ru.spbstu.kotlin.typeclass.TypeClasses
import kotlin.test.assertFailsWith

class SimpleTest {



    inline fun <E, reified T : Collection<E>> fee(v: T) = typeOf { v }.supertypes

    @Test
    fun kudos() {
        val tp = typeOf { arbitrary(listOf(2)) }
        println(tp)
        println(tp.supertypes)

        //println(fee(listOf(1, 2)))
    }

    @Test
    fun smokey() {
        assertFailsWith<ForInputException> {
            KCheck.forAll { lst: List<@InRange(-100, 100) Int> ->
                require(lst.zipWithNext().none { it.first == it.second })
            }
        }

        assertFailsWith<ForInputException> {
            KCheck.forAll(appendable = System.err) { lst: List<@InRange(-100, 100) Int> ->
                require(lst.isEmpty() || lst.any { it % 2 == 0 })
            }
        }

        assertFailsWith<ForInputException> {
            KCheck.forAll(appendable = System.out) { pr: Pair<@FromAlphabet("{}!@!#$%") String, @FromAlphabet("{}!@!#$%") String> ->
                require(pr.first.isEmpty() || pr.second.isEmpty() || !pr.first.contains(pr.second))
            }
        }

        assertFailsWith<ForInputException> {
            KCheck.forAll(appendable = System.out) { i: @InRange(0, 1000) Int, d: Double, s: String -> require(1 != s.length) }
        }
    }

    data class Quople<A, B, C>(val a: A, val b: B)

    @Test
    fun quople() {
        with(TypeClasses) {
            deriveArbitraryForDataClass(Quople::class)
            deriveShrinkerForDataClass(Quople::class)
        }

        assertFailsWith<ForInputException> {
            KCheck.forAll(appendable = System.out) { q: Quople<@SizeInRange(3, 12) String, @InRange(3, 12) Int?, Double> ->
                require(q.a.length != q.b)
            }
        }
    }

}