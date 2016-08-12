package ru.spbstu.kotlin.generate

import com.sun.org.apache.xpath.internal.operations.Bool
import ru.spbstu.kotlin.generate.combinators.Gen
import ru.spbstu.kotlin.generate.combinators.gen
import ru.spbstu.kotlin.generate.combinators.map
import ru.spbstu.kotlin.reflection.quasi.TypeHolder
import ru.spbstu.kotlin.reflection.quasi.buildTH
import ru.spbstu.kotlin.reflection.quasi.java.javaTypeOf
import java.lang.reflect.Type
import java.util.*
import kotlin.reflect.jvm.reflect

abstract class GenContext(val random: Random = Random()) {

    val default = HashMap<TypeHolder, Gen<*>>()
    val generic1 = HashMap<TypeHolder, (Gen<*>) -> Gen<*>>()
    val generic2 = HashMap<TypeHolder, (Gen<*>, Gen<*>) -> Gen<*>>()
    val generic3 = HashMap<TypeHolder, (Gen<*>, Gen<*>, Gen<*>) -> Gen<*>>()
    val generic4 = HashMap<TypeHolder, (Gen<*>, Gen<*>, Gen<*>, Gen<*>) -> Gen<*>>()

    fun <T> constant(value: T) = gen(value)

    fun anyInt() = gen { random.nextInt() }
    fun anyLong() = gen { random.nextLong() }
    fun anyShort() = gen { random.nextInt().toShort() }
    fun anyByte() = gen { random.nextInt().toByte() }
    fun anyBool() = gen { random.nextBoolean() }
    fun anyDouble() = gen { random.nextDouble() }
    fun anyFloat() = gen { random.nextFloat() }
    fun anyChar() = anyShort().map { it.toChar() }

    fun anyCommonChar() = gen {
        val common = "~!@#$%^&*()_+`1234567890-=QWERTYUIOP{}|qwertyuiop[]\\ASDFGHJKL:\"asdfghjkl;'ZXCVBNM<>?zxcvbnm,./ \t\n"
        common[random.nextInt(common.length)]
    }

    fun <T> priorities(vararg vs: Pair<Int, Gen<T>>) = run {
        var partialSum = 0;
        val res = vs.asSequence().map { pr -> partialSum += pr.first; (partialSum to pr.second) }.toList()
        val resultSum = partialSum
        gen {
            val peeker = random.nextInt(resultSum)
            res.find { it.first > peeker }!!.second.nextValue()
        }
    }

    fun defaultForInt() = priorities(
            1 to constant(0),
            1 to constant(1),
            1 to constant(Int.MAX_VALUE),
            1 to constant(Int.MIN_VALUE),
            3 to anyInt()
    )

    fun defaultForLong() = priorities(
            1 to constant(0L),
            1 to constant(1L),
            1 to constant(Long.MAX_VALUE),
            1 to constant(Long.MIN_VALUE),
            3 to anyLong()
    )

    fun defaultForShort() = priorities(
            1 to constant(0.toShort()),
            1 to constant(1.toShort()),
            1 to constant(Short.MAX_VALUE),
            1 to constant(Short.MIN_VALUE),
            3 to anyShort()
    )

    fun defaultForByte() = priorities(
            1 to constant(0.toByte()),
            1 to constant(1.toByte()),
            1 to constant(Byte.MAX_VALUE),
            1 to constant(Byte.MIN_VALUE),
            3 to anyByte()
    )

    fun defaultForBoolean() = anyBool()

    fun defaultForFloat() = priorities(
            1 to constant(0.0f),
            1 to constant(Float.MAX_VALUE),
            1 to constant(Float.MIN_VALUE),
            1 to constant(Float.NEGATIVE_INFINITY),
            1 to constant(Float.POSITIVE_INFINITY),
            1 to constant(Float.NaN),
            1 to constant(Math.ulp(0.0f)),
            1 to constant(Math.ulp(1.0f)),
            1 to constant(-Math.ulp(0.0f)),
            1 to constant(-Math.ulp(1.0f)),
            10 to anyFloat()
    )

    fun defaultForDouble() = priorities(
            1 to constant(0.0),
            1 to constant(Double.MAX_VALUE),
            1 to constant(Double.MIN_VALUE),
            1 to constant(Double.NEGATIVE_INFINITY),
            1 to constant(Double.POSITIVE_INFINITY),
            1 to constant(Double.NaN),
            1 to constant(Math.ulp(0.0)),
            1 to constant(Math.ulp(1.0)),
            1 to constant(-Math.ulp(0.0)),
            1 to constant(-Math.ulp(1.0)),
            10 to anyDouble()
    )

    fun defaultForNumber() = priorities(
            1 to defaultForInt().map { it as Number },
            1 to defaultForLong().map { it as Number },
            1 to defaultForShort().map { it as Number },
            1 to defaultForDouble().map { it as Number },
            1 to defaultForFloat().map { it as Number }
    )

    fun anyReadableString() =
            priorities(
                    1 to constant(""),
                    1 to anyCommonChar().map { it.toString() },
                    3 to gen {
                        val len = random.nextInt(20)
                        IntRange(0, len).map { anyCommonChar().nextValue() }.joinToString("")
                    },
                    3 to gen {
                        val len = random.nextInt(255)
                        IntRange(0, len).map { anyCommonChar().nextValue() }.joinToString("")
                    }
            )

    fun defaultForString() = priorities(
            1 to constant(""),
            1 to anyCommonChar().map { it.toString() },
            3 to gen {
                val len = random.nextInt(20)
                IntRange(0, len).map { anyCommonChar().nextValue() }.joinToString("")
            },
            3 to gen {
                val len = random.nextInt(255)
                IntRange(0, len).map { anyCommonChar().nextValue() }.joinToString("")
            },
            1 to gen {
                val len = random.nextInt(20)
                IntRange(0, len).map { anyChar().nextValue() }.joinToString("")
            },
            1 to gen {
                val len = random.nextInt(255)
                IntRange(0, len).map { anyChar().nextValue() }.joinToString("")
            }
    )

    fun <T> defaultForNullable(pure: Gen<T>): Gen<T?> = priorities(
            1 to constant(null),
            20 to pure
    )

    fun <T> defaultForArray(element: TypeHolder, elGen: Gen<T>): Gen<Array<T>> =
            priorities(
                    1 to gen { java.lang.reflect.Array.newInstance(element.clazz, 0) },
                    1 to gen {
                        val arr = java.lang.reflect.Array.newInstance(element.clazz, 1)
                        java.lang.reflect.Array.set(arr, 0, elGen.nextValue())
                        arr
                    },
                    3 to gen {
                        val len = random.nextInt(20)
                        val arr = java.lang.reflect.Array.newInstance(element.clazz, len)
                        IntRange(0, len - 1).forEach { java.lang.reflect.Array.set(arr, it, elGen.nextValue()) }
                        arr
                    },
                    3 to gen {
                        val len = random.nextInt(255)
                        val arr = java.lang.reflect.Array.newInstance(element.clazz, len)

                        IntRange(0, len - 1).forEach { java.lang.reflect.Array.set(arr, it, elGen.nextValue()) }
                        arr
                    }
            ) as Gen<Array<T>>


    fun <T> defaultForList(elGen: Gen<T>): Gen<List<T>> =
            priorities(
                    1 to gen { listOf<T>() },
                    1 to gen { listOf(elGen.nextValue()) },
                    3 to gen {
                        val len = random.nextInt(20)
                        IntRange(0, len).map { elGen.nextValue() }.toList()
                    },
                    3 to gen {
                        val len = random.nextInt(255)
                        IntRange(0, len).map { elGen.nextValue() }.toList()
                    },
                    1 to gen {
                        val len = random.nextInt(3000)
                        IntRange(0, len).map { elGen.nextValue() }.toList()
                    }
            )

    fun <T> defaultForSet(elGen: Gen<T>): Gen<Set<T>> =
            priorities(
                    1 to gen { setOf<T>() },
                    1 to gen { setOf(elGen.nextValue()) },
                    3 to gen {
                        val len = random.nextInt(20)
                        IntRange(0, len).map { elGen.nextValue() }.toSet()
                    },
                    3 to gen {
                        val len = random.nextInt(255)
                        IntRange(0, len).map { elGen.nextValue() }.toSet()
                    },
                    1 to gen {
                        val len = random.nextInt(3000)
                        IntRange(0, len).map { elGen.nextValue() }.toSet()
                    }
            )

    @JvmName("installGenerator")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: () -> G) {
        val type = buildTH<G>(function).arguments.first()
        val gen = function()
        default[type] = gen
        if (!type.isNullable) {
            default[type.copy(isNullable = true)] = defaultForNullable(gen)
        }
    }

    @JvmName("installFunction")
    inline fun <reified T> install(noinline function: () -> T) {
        val type = buildTH<T>(function)
        val gen = gen(function)
        default[type] = gen
        if (!type.isNullable) {
            default[type.copy(isNullable = true)] = defaultForNullable(gen)
        }
    }

    inline fun <reified T, R> feed(noinline function: (T) -> R): R {
        val type = buildTH<T>(function.reflect()?.parameters?.first()?.type!!)

        return function(default[type]?.nextValue() as T)
    }

    inline fun <reified T> forAll(tries: Int = 100, noinline function: (T) -> Boolean) =
            (0..tries).fold(true) { acc, v -> acc and feed(function) }


}

object Gens : GenContext() {
    init {
        install { defaultForBoolean() }
        install { defaultForDouble() }
        install { defaultForFloat() }
        install { defaultForByte() }
        install { defaultForShort() }
        install { defaultForInt() }
        install { defaultForLong() }
        install { defaultForString() }
    }
}
