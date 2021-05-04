package ru.spbstu.kotlin.generate.combinators

import ru.spbstu.kotlin.generate.*
import ru.spbstu.kotlin.generate.arbitraries.*
import ru.spbstu.kotlin.generate.util.*
import ru.spbstu.kotlin.typeclass.TCKind
import ru.spbstu.kotlin.typeclass.TypeClasses
import ru.spbstu.ktuples.Tuple
import ru.spbstu.ktuples.mapEach

interface Arbitrary<out T> : TCKind<Arbitrary<*>, @UnsafeVariance T> {
    companion object : GenerationContext()
    fun next(): T
}

open class GenerationContext {
    val random = HighQualityRandom()

    fun defaultForInt(annotations: List<Annotation>) =
        when(val range = annotations.firstOrNull { it is InRange } as? InRange) {
            null -> priorities(
                    1 to constant(0),
                    1 to constant(1),
                    1 to constant(Int.MAX_VALUE),
                    1 to constant(Int.MIN_VALUE),
                    3 to anyInt()
            )
            else -> {
                val pris = mutableListOf<Pair<Int, Arbitrary<Int>>>()

                if(0 in range) pris += 1 to constant(0)
                if(1 in range) pris += 1 to constant(1)
                pris += 1 to constant(range.from.toInt())
                pris += 1 to constant(range.to.toInt())
                pris += 3 to anyInt(range.from.toInt(), range.to.toInt())

                priorities(*pris.toTypedArray())
            }
        }

    fun defaultForLong(annotations: List<Annotation>) =
            when(val range = annotations.firstOrNull { it is InRange } as? InRange) {
                null -> priorities(
                    1 to constant(0L),
                    1 to constant(1L),
                    1 to constant(Long.MAX_VALUE),
                    1 to constant(Long.MIN_VALUE),
                    3 to anyLong()
                )
                else -> {
                    val pris = mutableListOf<Pair<Int, Arbitrary<Long>>>()

                    if(0 in range) pris += 1 to constant(0L)
                    if(1 in range) pris += 1 to constant(1L)
                    pris += 1 to constant(range.from)
                    pris += 1 to constant(range.to)
                    pris += 3 to anyLong(range.from, range.to)

                    priorities(*pris.toTypedArray())
                }
            }

    fun defaultForShort(annotations: List<Annotation>) =
            when(val range = annotations.firstOrNull { it is InRange } as? InRange) {
                null -> priorities(
                        1 to constant(0.toShort()),
                        1 to constant(1.toShort()),
                        1 to constant(Short.MAX_VALUE),
                        1 to constant(Short.MIN_VALUE),
                        3 to anyShort()
                )
                else -> {
                    val pris = mutableListOf<Pair<Int, Arbitrary<Short>>>()

                    if(0 in range) pris += 1 to constant(0.toShort())
                    if(1 in range) pris += 1 to constant(1.toShort())
                    pris += 1 to constant(range.from.toShort())
                    pris += 1 to constant(range.to.toShort())
                    pris += 3 to anyShort(range.from.toShort(), range.to.toShort())

                    priorities(*pris.toTypedArray())
                }
            }

    fun defaultForByte(annotations: List<Annotation>) =
            when(val range = annotations.firstOrNull { it is InRange } as? InRange) {
                null -> priorities(
                        1 to constant(0.toByte()),
                        1 to constant(1.toByte()),
                        1 to constant(Byte.MAX_VALUE),
                        1 to constant(Byte.MIN_VALUE),
                        3 to anyByte()
                )
                else -> {
                    val pris = mutableListOf<Pair<Int, Arbitrary<Byte>>>()

                    if(0 in range) pris += 1 to constant(0.toByte())
                    if(1 in range) pris += 1 to constant(1.toByte())
                    pris += 1 to constant(range.from.toByte())
                    pris += 1 to constant(range.to.toByte())
                    pris += 3 to anyByte(range.from.toByte(), range.to.toByte())

                    priorities(*pris.toTypedArray())
                }
            }

    fun defaultForBoolean() = anyBool()

    fun defaultForFloat(annotations: List<Annotation>) = priorities(
            1 to constant(0.0f),
            1 to constant(Float.MAX_VALUE),
            1 to constant(-Float.MAX_VALUE),
            1 to constant(Float.NEGATIVE_INFINITY),
            1 to constant(Float.POSITIVE_INFINITY),
            1 to constant(Float.NaN),
            1 to constant(Math.ulp(0.0f)),
            1 to constant(Math.ulp(1.0f)),
            1 to constant(-Math.ulp(0.0f)),
            1 to constant(-Math.ulp(1.0f)),
            10 to anyFloat()
    )

    fun defaultForDouble(annotations: List<Annotation>) = priorities(
            1 to constant(0.0),
            1 to constant(Double.MAX_VALUE),
            1 to constant(-Double.MAX_VALUE),
            1 to constant(Double.NEGATIVE_INFINITY),
            1 to constant(Double.POSITIVE_INFINITY),
            1 to constant(Double.NaN),
            1 to constant(Math.ulp(0.0)),
            1 to constant(Math.ulp(1.0)),
            1 to constant(-Math.ulp(0.0)),
            1 to constant(-Math.ulp(1.0)),
            10 to anyDouble()
    )

    fun defaultForNumber(annotations: List<Annotation>) =
            priorities(
                    1 to defaultForInt(annotations),
                    1 to defaultForLong(annotations),
                    1 to defaultForShort(annotations),
                    1 to defaultForDouble(annotations),
                    1 to defaultForFloat(annotations)
            )

    fun defaultForChar(annotations: List<Annotation>): Arbitrary<Char> {
        val charRange: InCharRange? = annotations.filterIsInstance<InCharRange>().firstOrNull()
        val fromAlphabet: FromAlphabet? = annotations.filterIsInstance<FromAlphabet>().firstOrNull()

        val alphabet = fromAlphabet?.alphabet ?: charRange?.run { (from..to).joinToString("") }

        return when(alphabet) {
            null -> priorities(
                    2 to anyCommonChar(),
                    1 to anyChar()
            )
            else -> anyChar(alphabet)
        }
    }

    fun defaultSize(annotations: List<Annotation>): Arbitrary<Int> {
        return when(val range = annotations.filterIsInstance<SizeInRange>().firstOrNull()) {
            null -> priorities(
                1 to constant(0),
                1 to constant(1),
                3 to anyInt(1, 20),
                3 to anyInt(1, 255),
                1 to anyInt(1, 5000)
            )
            else -> {
                val pris = mutableListOf<Pair<Int, Arbitrary<Int>>>()
                if(0 in range && 0 != range.from) pris += 1 to constant(0)
                if(1 in range && 1 != range.from) pris += 1 to constant(1)
                pris += 1 to constant(range.from)
                pris += 3 to anyInt(range.from, range.from + (range.to - range.from) / 200)
                pris += 3 to anyInt(range.from, range.from + (range.to - range.from) / 50)
                pris += 1 to anyInt(range.from, range.to)
                priorities(*pris.toTypedArray())
            }
        }
    }

    fun defaultForString(annotations: List<Annotation>) =
            anyString(defaultForChar(annotations), defaultSize(annotations))

    fun <T> defaultForNullable(pure: Arbitrary<T>) = anyNullable(pure)

    fun <T> defaultForList(elGen: Arbitrary<T>, annotations: List<Annotation>): Arbitrary<List<T>> {
        return anyList(elGen, defaultSize(annotations))
    }
    fun <T> defaultForSet(elGen: Arbitrary<T>, annotations: List<Annotation>): Arbitrary<Set<T>> =
            anySet(elGen, defaultSize(annotations))
    fun <K, V> defaultForMap(kGen: Arbitrary<K>, vGen: Arbitrary<V>, annotations: List<Annotation>): Arbitrary<Map<K, V>> =
            anyMap(kGen, vGen, defaultSize(annotations))
    fun <A, B> defaultForPair(aGen: Arbitrary<A>, bGen: Arbitrary<B>): Arbitrary<Pair<A, B>> =
            anyPair(aGen, bGen)
    fun <A, B, C> defaultForTriple(aGen: Arbitrary<A>, bGen: Arbitrary<B>, cGen: Arbitrary<C>): Arbitrary<Triple<A, B, C>> =
            anyTriple(aGen, bGen, cGen)

    fun exportDefaults() {
        with(TypeClasses) {
            val arbitrary = Arbitrary::class

            instance { (e) -> defaultForNullable(arbitrary of e) }
            instance { -> defaultForBoolean() }
            instance { _, annos -> defaultForDouble(annos) }
            instance { _, annos -> defaultForFloat(annos) }
            instance { _, annos -> defaultForByte(annos) }
            instance { _, annos -> defaultForShort(annos) }
            instance { _, annos -> defaultForInt(annos) }
            instance { _, annos -> defaultForLong(annos) }
            instance { _, annos -> defaultForChar(annos) }
            instance { _, annos -> defaultForString(annos) }

            instance { _, annos -> defaultForNumber(annos) }

            instance { (arg), annos -> defaultForList(arbitrary of arg, annos) }
            instance { (arg), annos -> defaultForSet(arbitrary of arg, annos) }
            instance { (arg1, arg2), annos -> defaultForMap(arbitrary of arg1, arbitrary of arg2, annos) }

            instance { (arg1, arg2) -> defaultForPair(
                    arbitrary of arg1,
                    arbitrary of arg2
            ) }
            instance { (arg1, arg2, arg3) -> defaultForTriple(
                    arbitrary of arg1,
                    arbitrary of arg2,
                    arbitrary of arg3
            ) }

            instance { _, annos -> defaultForString(annos) as Arbitrary<CharSequence> }
            instance { (arg), annos -> defaultForList(arbitrary of arg, annos) as Arbitrary<Collection<*>> }
            instance { (arg), annos -> defaultForList(arbitrary of arg, annos) as Arbitrary<Iterable<*>> }


            instance { (a), annos ->
                val als = Tuple(a).mapEach { arbitrary of it }
                arbitrary { als.mapEach { it.next() } }
            }
            instance { (a, b), annos ->
                val als = Tuple(a, b).mapEach { arbitrary of it }
                arbitrary { als.mapEach { it.next() } }
            }
            instance { (a, b, c), annos ->
                val als = Tuple(a, b, c).mapEach { arbitrary of it }
                arbitrary { als.mapEach { it.next() } }
            }
            instance { (a, b, c, d), annos ->
                val als = Tuple(a, b, c, d).mapEach { arbitrary of it }
                arbitrary { als.mapEach { it.next() } }
            }
            instance { (a, b, c, d, e), annos ->
                val als = Tuple(a, b, c, d, e).mapEach { arbitrary of it }
                arbitrary { als.mapEach { it.next() } }
            }
            instance { (a, b, c, d, e, f), annos ->
                val als = Tuple(a, b, c, d, e, f).mapEach { arbitrary of it }
                arbitrary { als.mapEach { it.next() } }
            }

        }
    }

}

inline fun <T> arbitrary(crossinline f: () -> T) = object: Arbitrary<T> {
    override fun next() = f()
}

fun <T> arbitrary(v: T) = object: Arbitrary<T> {
    override fun next() = v
}

inline fun<T, U> Arbitrary<T>.map(crossinline f: (T) -> U) = arbitrary { f(next()) }

inline fun<A, B, R> Arbitrary<A>.zip(that: Arbitrary<B>, crossinline f: (A, B) -> R) =
        arbitrary { f(this.next(), that.next()) }

fun <T> Iterator<T>.asArbitrary() = arbitrary { next() }
fun <T> Sequence<T>.asArbitrary() = object: Arbitrary<T> {
    val it = iterator()
    override fun next() = it.next()
}

fun<E> GenerationContext.oneOf(vararg variants: E) = variants[random.nextInt(variants.size)]
fun<E> GenerationContext.oneOf(variants: List<E>) = variants[random.nextInt(variants.size)]
@JvmName("oneOfArray")
fun<E> GenerationContext.oneOf(variants: Array<E>) = variants[random.nextInt(variants.size)]
fun GenerationContext.oneOf(variants: CharSequence) = variants[random.nextInt(variants.length)]

operator fun <T> Arbitrary<T>.iterator() = iterator {
    while(true) yield(next())
}
