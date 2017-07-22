package ru.spbstu.kotlin.generate.combinators

@FunctionalInterface
interface Shrinker<T> {
    fun shrink(input: T): Sequence<T> = emptySequence()
}

inline fun<T> shrinker(crossinline f: (T) -> Sequence<T>) = object: Shrinker<T> {
    override fun shrink(input: T): Sequence<T> = f(input)
}

