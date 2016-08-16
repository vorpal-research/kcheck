package ru.spbstu.kotlin.generate.combinators

import java.util.stream.Stream

@FunctionalInterface
interface Gen<out T> {
    fun nextValue(): T
}

inline fun <T> gen(crossinline f: () -> T) = object: Gen<T> {
    override fun nextValue() = f()
}

fun <T> gen(v: T) = object: Gen<T> {
    override fun nextValue() = v
}

inline fun<T, U> Gen<T>.map(crossinline f: (T) -> U) = gen { f(nextValue()) }

inline fun<A, B, R> Gen<A>.zip(that: Gen<B>, crossinline f: (A, B) -> R) = gen { f(this.nextValue(), that.nextValue()) }

fun <T> Iterator<T>.asGen() = gen{ next() }
fun <T> Sequence<T>.asGen() = object: Gen<T> {
    val it = iterator()
    override fun nextValue() = it.next()
}
fun <T> Stream<T>.asGen() = object: Gen<T> {
    val it = iterator()
    override fun nextValue() = it.next()
}
