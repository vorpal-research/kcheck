package ru.spbstu.kotlin.generate.arbitraries

import ru.spbstu.kotlin.generate.combinators.Arbitrary
import ru.spbstu.kotlin.generate.combinators.GenerationContext
import ru.spbstu.kotlin.generate.combinators.arbitrary
import java.util.*

fun <T> GenerationContext.constant(value: T) = arbitrary(value)

fun <T> GenerationContext.priorities(vararg vs: Pair<Int, Arbitrary<T>>) = run {
    val recalc: NavigableMap<Int, Arbitrary<T>> = TreeMap()
    var sum: Int = 0
    for((k, v) in vs) {
        recalc[sum] = v
        sum += k
    }
    arbitrary {
        val i = random.nextInt(sum)
        recalc.floorEntry(i).value.next()
    }
}

