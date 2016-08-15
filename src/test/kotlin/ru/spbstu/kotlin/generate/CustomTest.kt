package ru.spbstu.kotlin.generate

import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomTest{

    data class Data(val x: Int, val y: List<Double?>)

    data class Recursive(val x: Int, val y: Int, val recurse: Recursive?)

    val Recursive?.size: Int get() = when(this){ null -> 0; else -> recurse.size + 1}

    @Before
    fun beforeAll() {
        Gens.installFunction(::Data)
        Gens.installFunction(::Recursive)
    }

    @Test
    fun testStuff() {
        assertFalse(Gens.forAll { d: List<Data?> ->
            d.all { it != null }
        })

        assertTrue(Gens.forAll { r: Recursive ->
            r.size > 0
        })

        assertFalse(Gens.forAll { r: Recursive? ->
            r.size > 0
        })
    }
}