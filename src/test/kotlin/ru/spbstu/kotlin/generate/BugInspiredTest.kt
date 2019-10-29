package ru.spbstu.kotlin.generate

import org.junit.Test
import ru.spbstu.kotlin.generate.combinators.ForInputException
import ru.spbstu.kotlin.generate.combinators.KCheck
import kotlin.test.assertFailsWith

class BugInspiredTest {
    @Test
    fun `shrinker for map should eventually stop`() {
        fun isMapGoodEnough(map: Map<String, String>) = false

        assertFailsWith<ForInputException> {
            KCheck.forAll { map: Map<String, String> ->
                require(isMapGoodEnough(map))
            }
        }
    }

    @Test
    fun `shrinker for set should eventually stop`() {
        fun isSetGoodEnough(set: Set<String>) = false

        assertFailsWith<ForInputException> {
            KCheck.forAll { set: Set<String> ->
                require(isSetGoodEnough(set))
            }
        }
    }
}