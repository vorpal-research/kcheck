package ru.spbstu.kotlin.generate

import org.junit.Test
import ru.spbstu.kotlin.generate.context.Gens
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BaseTest {
    @Test
    fun testSimple() {
        assertFalse(Gens.forAll { d: Double -> d == d })
        // NaN == NaN used to be true for type Double?
        //assertTrue(Gens.forAll { d: Double? -> d == d }) // haha
        assertFalse(Gens.forAll { i: Int -> (i + 1) > i })
        assertFalse(Gens.forAll { t1: Double, t2: Double? -> t1 != t2 })
        assertTrue(Gens.forAll { d: Collection<Pair<Double?, String>> -> d == d })
        assertFalse(Gens.forAll { c1: List<Int?> -> c1.toSet().size == c1.size  })
        
        //assertFalse(Gens.forAll { arr: Array<Double> -> arr.sum() < 3.0 })
    }

}