package ru.spbstu.kotlin.generate.combinators

@FunctionalInterface
interface Shrinker<T> {
    fun shrink(input: T): Iterable<T> = emptyList()
}

inline fun<T> shrinker(crossinline f: (T) -> Iterable<T>) = object: Shrinker<T> {
    override fun shrink(input: T): Iterable<T> = f(input)
}

