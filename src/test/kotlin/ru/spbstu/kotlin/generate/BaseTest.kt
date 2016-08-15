package ru.spbstu.kotlin.generate

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BaseTest {
    @Test
    fun testSimple() {
        assertFalse(Gens.forAll { d: Double -> d == d })
        assertTrue(Gens.forAll { d: Double? -> d == d }) // haha
        assertFalse(Gens.forAll { i: Int -> (i + 1) > i })
        assertFalse(Gens.forAll { t1: Double, t2: Double? -> t1 != t2 })
        assertTrue(Gens.forAll { d: Collection<Pair<Double?, String>> -> d == d })
        assertFalse { Gens.forAll { c1: List<Int?> -> c1.toSet().size == c1.size  } }
    }
}