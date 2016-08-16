package ru.spbstu.kotlin.generate.cases

import ru.spbstu.kotlin.generate.context.GenContext
import ru.spbstu.kotlin.generate.cases.constant
import ru.spbstu.kotlin.generate.cases.priorities
import ru.spbstu.kotlin.generate.combinators.Gen
import ru.spbstu.kotlin.generate.combinators.gen
import ru.spbstu.kotlin.generate.combinators.map

fun GenContext.anyInt() = gen { random.nextInt() }
fun GenContext.anyLong() = gen { random.nextLong() }
fun GenContext.anyShort() = gen { random.nextInt().toShort() }
fun GenContext.anyByte() = gen { random.nextInt().toByte() }
fun GenContext.anyBool() = gen { random.nextBoolean() }
fun GenContext.anyDouble() = gen { random.nextDouble() }
fun GenContext.anyFloat() = gen { random.nextFloat() }
fun GenContext.anyChar() = anyShort().map { it.toChar() }

fun GenContext.anyCommonChar() = gen {
    val common = "~!@#$%^&*()_+`1234567890-=QWERTYUIOP{}|qwertyuiop[]\\ASDFGHJKL:\"asdfghjkl;'ZXCVBNM<>?zxcvbnm,./ \t\n"
    common[random.nextInt(common.length)]
}

fun<T> GenContext.anyNullable(pure: Gen<T>): Gen<T?> =
        priorities(
                20 to pure,
                1 to constant(null)
        )

fun GenContext.anyNumber(): Gen<Number> =
        priorities(
                1 to anyInt(),
                1 to anyLong(),
                1 to anyShort(),
                1 to anyDouble(),
                1 to anyFloat()
        )
