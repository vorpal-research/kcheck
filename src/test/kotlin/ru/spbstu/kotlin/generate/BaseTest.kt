package ru.spbstu.kotlin.generate

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun <T> T.trace(): T = apply { println(this) }

class BaseTest {
    @Test
    fun testSimple() {
        assertTrue(Gens.forAll { d: Double? -> d.trace() == d })

        assertTrue(Gens.forAll { d: List<Double?> -> d.trace() == d })
    }
}