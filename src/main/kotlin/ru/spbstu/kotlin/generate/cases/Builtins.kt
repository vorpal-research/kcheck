package ru.spbstu.kotlin.generate.cases

import ru.spbstu.kotlin.generate.combinators.Gen
import ru.spbstu.kotlin.generate.combinators.gen
import ru.spbstu.kotlin.generate.combinators.map
import ru.spbstu.kotlin.generate.context.GenContext
import ru.spbstu.kotlin.generate.util.ReflectedArray
import ru.spbstu.kotlin.generate.util.nextInRange

fun GenContext.anyInt() = gen { random.nextInt() }
fun GenContext.anyInt(min: Int, max: Int) = gen { random.nextInRange(min, max) }
fun GenContext.anyLong() = gen { random.nextLong() }
fun GenContext.anyLong(min: Long, max: Long) = gen { random.nextInRange(min, max) }
fun GenContext.anyShort() = gen { random.nextInt().toShort() }
fun GenContext.anyShort(min: Short, max: Short) = gen { random.nextInRange(min, max) }
fun GenContext.anyByte() = gen { random.nextInt().toByte() }
fun GenContext.anyByte(min: Byte, max: Byte) = gen { random.nextInRange(min, max) }
fun GenContext.anyBool() = gen { random.nextBoolean() }
fun GenContext.anyDouble() = gen { random.nextDouble() }
fun GenContext.anyDouble(min: Double, max: Double) = gen { random.nextInRange(min, max) }
fun GenContext.anyFloat() = gen { random.nextFloat() }
fun GenContext.anyFloat(min: Float, max: Float) = gen { random.nextInRange(min, max) }
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

fun <T> GenContext.anyArray(jclass: Class<T>, elGen: Gen<T>): Gen<Array<T>> =
        priorities(
                1 to gen { ReflectedArray(jclass, 0).array },
                1 to gen { ReflectedArray(jclass, 1){ elGen.nextValue() }.array },
                3 to gen { ReflectedArray(jclass, random.nextInt(20)){ elGen.nextValue() }.array },
                3 to gen { ReflectedArray(jclass, random.nextInt(255)){ elGen.nextValue() }.array }
        )
