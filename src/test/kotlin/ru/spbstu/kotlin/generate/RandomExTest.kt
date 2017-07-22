package ru.spbstu.kotlin.generate

import org.junit.Test
import ru.spbstu.kotlin.generate.util.*
import java.util.*
import kotlin.test.assertTrue

class RandomExTest {

    @Test
    fun `nextLong(bound) should return values in 0 -- bound`() {
        val random = Random(0L)
        val gens = generateSequence { random.nextLong(10L) }.take(1000).toSet()
        assertTrue(0L in gens)
        assertTrue(10L !in gens)
        assertTrue(9L in gens)
        assertTrue(gens.all { it in 0..10 })
    }

    @Test
    fun `nextShort(bound) should return values in 0 -- bound`() {
        val random = Random(0L)
        val gens = generateSequence { random.nextShort(10) }.take(1000).toSet()
        assertTrue(0 in gens)
        assertTrue(10 !in gens)
        assertTrue(9 in gens)
        assertTrue(gens.all { it in 0..10 })
    }

    @Test
    fun `nextByte(bound) should return values in 0 -- bound`() {
        val random = Random(0L)
        val gens = generateSequence { random.nextByte(10) }.take(1000).toSet()
        assertTrue(0 in gens)
        assertTrue(10 !in gens)
        assertTrue(9 in gens)
        assertTrue(gens.all { it in 0..10 })
    }

    @Test
    fun `nextByte() should return values in (-128) -- 127`() {
        val random = Random(0L)
        val gens = generateSequence { random.nextByte() }.take(1000).toSet()
        assertTrue(Byte.MIN_VALUE in gens)
        assertTrue(Byte.MAX_VALUE in gens)
    }

    @Test
    fun `nextInRange(a, b) should return values in a -- b (Byte version)`() {
        val random = Random(0L)
        val gens = generateSequence { random.nextInRange((-5).toByte(), 42.toByte()) }.take(1000).toSet()
        assertTrue(-5 in gens)
        assertTrue(42 !in gens)
        assertTrue(41 in gens)
        assertTrue(gens.all { it in (-5)..42 })
    }

    @Test
    fun `nextInRange(a, b) should return values in a -- b (Short version)`() {
        val random = Random(0L)
        val gens = generateSequence { random.nextInRange((-5).toShort(), 42.toShort()) }.take(1000).toSet()
        assertTrue(-5 in gens)
        assertTrue(42 !in gens)
        assertTrue(41 in gens)
        assertTrue(gens.all { it in (-5)..42 })
    }

    @Test
    fun `nextInRange(a, b) should return values in a -- b (Int version)`() {
        val random = Random(0L)
        val gens = generateSequence { random.nextInRange((-5), 42) }.take(1000).toSet()
        assertTrue(-5 in gens)
        assertTrue(42 !in gens)
        assertTrue(41 in gens)
        assertTrue(gens.all { it in (-5)..42 })
    }

    @Test
    fun `nextInRange(a, b) should return values in a -- b (Long version)`() {
        val random = Random(0L)
        val gens = generateSequence { random.nextInRange((-5L), 42L) }.take(1000).toSet()
        assertTrue(-5 in gens)
        assertTrue(42 !in gens)
        assertTrue(41 in gens)
        assertTrue(gens.all { it in (-5)..42 })
    }

    @Test
    fun `nextInRange(a, b) should return values in a -- b (Float version)`() {
        val random = Random(0L)
        val gens = generateSequence { random.nextInRange((-5.0f), 42.toFloat()) }.take(1000).toSet()
        assertTrue(gens.all { it in (-5.0)..42.0 })
    }

    @Test
    fun `nextInRange(a, b) should return values in a -- b (Double version)`() {
        val random = Random(0L)
        val gens = generateSequence { random.nextInRange((-5.0), 42.toDouble()) }.take(1000).toSet()
        assertTrue(gens.all { it in (-5.0)..42.0 })
    }

    @Test
    fun `nextString(alphabet) should return strings within alphabet`() {
        val random = Random(0L)
        val alphabet = ('a'..'z')
        val gens = generateSequence { random.nextString(alphabet.asCharSequence(), 3, 20) }.take(1000).toSortedSet()
        assertTrue(gens.all { str -> str.all { it in alphabet } })
        assertTrue(gens.all { it.length in 3..20 })
    }
}