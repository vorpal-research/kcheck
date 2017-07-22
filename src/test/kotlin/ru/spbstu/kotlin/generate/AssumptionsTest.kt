package ru.spbstu.kotlin.generate

import org.junit.Assert
import org.junit.Test
import ru.spbstu.kotlin.generate.assume.assume
import ru.spbstu.kotlin.generate.context.Gens

class AssumptionsTest {
    @Test
    fun testAssumptions() {
        var counter = 0
        Gens.forAll(2000) { x: Int ->
            assume(x > 0)
            Assert.assertTrue(x > 0)
            ++counter
            true
        }

        Assert.assertEquals(2000, counter)
    }
}