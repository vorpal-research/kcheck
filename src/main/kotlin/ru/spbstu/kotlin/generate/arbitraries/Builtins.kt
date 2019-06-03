package ru.spbstu.kotlin.generate.arbitraries

import ru.spbstu.kotlin.generate.combinators.Arbitrary
import ru.spbstu.kotlin.generate.combinators.GenerationContext
import ru.spbstu.kotlin.generate.combinators.arbitrary
import ru.spbstu.kotlin.generate.combinators.map
import ru.spbstu.kotlin.generate.util.nextInRange

fun GenerationContext.anyInt() = arbitrary { random.nextInt() }
fun GenerationContext.anyInt(min: Int, max: Int) = arbitrary { random.nextInRange(min, max) }
fun GenerationContext.anyLong() = arbitrary { random.nextLong() }
fun GenerationContext.anyLong(min: Long, max: Long) = arbitrary { random.nextInRange(min, max) }
fun GenerationContext.anyShort() = arbitrary { random.nextInt().toShort() }
fun GenerationContext.anyShort(min: Short, max: Short) = arbitrary { random.nextInRange(min, max) }
fun GenerationContext.anyByte() = arbitrary { random.nextInt().toByte() }
fun GenerationContext.anyByte(min: Byte, max: Byte) = arbitrary { random.nextInRange(min, max) }
fun GenerationContext.anyBool() = arbitrary { random.nextBoolean() }
fun GenerationContext.anyDouble() = arbitrary { random.nextDouble() }
fun GenerationContext.anyDouble(min: Double, max: Double) = arbitrary { random.nextInRange(min, max) }
fun GenerationContext.anyFloat() = arbitrary { random.nextFloat() }
fun GenerationContext.anyFloat(min: Float, max: Float) = arbitrary { random.nextInRange(min, max) }
fun GenerationContext.anyChar() = anyShort().map { it.toChar() }

fun GenerationContext.anyChar(alphabet: String) = anyInt(0, alphabet.lastIndex).map { alphabet[it] }

fun GenerationContext.anyCommonChar() = arbitrary {
    val common = "~!@#$%^&*()_+`1234567890-=QWERTYUIOP{}|qwertyuiop[]\\ASDFGHJKL:\"asdfghjkl;'ZXCVBNM<>?zxcvbnm,./ \t\n"
    common[random.nextInt(common.length)]
}

fun<T> GenerationContext.anyNullable(pure: Arbitrary<T>): Arbitrary<T?> =
        priorities(
                20 to pure,
                1 to constant(null)
        )

fun GenerationContext.anyNumber(): Arbitrary<Number> =
        priorities(
                1 to anyInt(),
                1 to anyLong(),
                1 to anyShort(),
                1 to anyDouble(),
                1 to anyFloat()
        )
