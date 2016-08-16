package ru.spbstu.kotlin.generate.cases

import ru.spbstu.kotlin.generate.context.GenContext
import ru.spbstu.kotlin.generate.combinators.Gen
import ru.spbstu.kotlin.generate.combinators.gen

fun <T> GenContext.constant(value: T) = gen(value)

fun <T> GenContext.priorities(vararg vs: Pair<Int, Gen<T>>) = run {
    var partialSum = 0;
    val res = vs.asSequence().map { pr -> partialSum += pr.first; (partialSum to pr.second) }.toList()
    val resultSum = partialSum
    gen {
        val peeker = random.nextInt(resultSum)
        res.find { it.first > peeker }!!.second.nextValue()
    }
}

