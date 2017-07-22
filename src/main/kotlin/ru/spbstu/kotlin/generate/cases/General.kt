package ru.spbstu.kotlin.generate.cases

import ru.spbstu.kotlin.generate.context.GenContext
import ru.spbstu.kotlin.generate.combinators.Gen
import ru.spbstu.kotlin.generate.combinators.gen
import java.util.*

fun <T> GenContext.constant(value: T) = gen(value)

fun <T> GenContext.priorities(vararg vs: Pair<Int, Gen<T>>) = run {
    val recalc: NavigableMap<Int, Gen<T>> = TreeMap()
    var sum: Int = 0
    for((k, v) in vs) {
        recalc[sum] = v
        sum += k
    }
    gen {
        val i = random.nextInt(sum)
        recalc.floorEntry(i).value.nextValue()
    }
}

