package ru.spbstu.kotlin.generate.util

object FancyFunctions {
    fun <R1, R2> (() -> R1).mapResult(mapper: (R1) -> R2): () -> R2 =
            { mapper(this()) }
    fun <A1, R1, R2> ((A1) -> R1).mapResult(mapper: (R1) -> R2): (A1) -> R2 =
            { a1 -> mapper(this(a1)) }
    fun <A1, A2, R1, R2> ((A1, A2) -> R1).mapResult(mapper: (R1) -> R2): (A1, A2) -> R2 =
            { a1, a2 -> mapper(this(a1, a2)) }
    fun <A1, A2, A3, R1, R2> ((A1, A2, A3) -> R1).mapResult(mapper: (R1) -> R2): (A1, A2, A3) -> R2 =
            { a1, a2, a3 -> mapper(this(a1, a2, a3)) }
    fun <A1, A2, A3, A4, R1, R2> ((A1, A2, A3, A4) -> R1).mapResult(mapper: (R1) -> R2): (A1, A2, A3, A4) -> R2 =
            { a1, a2, a3, a4 -> mapper(this(a1, a2, a3, a4)) }
}
