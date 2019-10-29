package ru.spbstu.kotlin.generate.combinators

import ru.spbstu.kotlin.generate.*
import ru.spbstu.kotlin.generate.util.firstInstanceOf
import ru.spbstu.kotlin.typeclass.TCKind
import ru.spbstu.kotlin.typeclass.TypeClasses
import ru.spbstu.ktuples.*
import ru.spbstu.wheels.transpose
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.reflect.KType

fun abs(s: Short) = if(s < 0) (-s).toShort() else s
fun abs(b: Byte) = if(b < 0) (-b).toByte() else b

interface Shrinker<T> : TCKind<Shrinker<*>, T> {
    companion object : ShrinkerContext()
    fun shrink(input: T): Sequence<T> = emptySequence()
}

inline fun<T> shrinker(crossinline f: (T) -> Sequence<T>) = object: Shrinker<T> {
    override fun shrink(input: T): Sequence<T> = f(input)
}

fun <T, R> Shrinker<T>.bimap(fwd: (T) -> R, bwd: (R) -> T) = shrinker<R> { shrink(bwd(it)).map(fwd) }

fun <T> Iterable<Sequence<T>>.transpose(): Sequence<List<T>> = sequence {
    val iters = map { it.iterator() }
    require(iters.isNotEmpty())
    while(iters.all { it.hasNext() }) {
        yield(iters.map { it.next() })
    }
}

fun <T, C: MutableCollection<T>> Iterable<Sequence<T>>.transposeTo(collectionBuilder: () -> C): Sequence<C> = sequence {
    val iters = map { it.iterator() }
    require(iters.isNotEmpty())
    while(iters.all { it.hasNext() }) {
        yield(iters.mapTo(collectionBuilder()) { it.next() })
    }
}

fun <T> shrinkIterable(v: Iterable<T>, shrinker: Shrinker<T>): Sequence<List<T>> =
        v.map { shrinker.shrink(it) }.transpose() +
                v.mapIndexed { i, t -> shrinker.shrink(t).map { e -> v.toMutableList().apply { set(i, e) } } }
                        .asSequence().flatten()

fun <T> shrinkMany(vs: Iterable<T>, shrinkers: Iterable<Shrinker<T>>): Sequence<List<T>> =
        vs.zip(shrinkers) { v, sh -> sh.shrink(v) }.transpose() +
                vs.zip(shrinkers).mapIndexed { i, (v, sh) ->
                    sh.shrink(v).map { e -> vs.toMutableList().apply { set(i, e) } }
                }.asSequence().flatten()

open class ShrinkerContext {
    fun defaultForInt(annos: List<Annotation>) = shrinker { i: Int ->
        val range = annos.firstInstanceOf<InRange>()
        fun check(value: Int) = range == null || value in range
        fun adjust(value: Int) = value // this is here only to simplify copy-pasting these (yes, really)
        fun adjust(value: Long) = value.toInt()

        sequence {
            if(i == adjust(0)) return@sequence
            yield(adjust(0))

            if(i == adjust(1)) return@sequence
            yield(adjust(1))

            if(range != null && i != adjust(range.from)) yield(adjust(range.from))

            if(i < 0) yield(abs(i))

            yield(adjust(i / 2))
            yield(adjust(i / 2 + 1))
        }.filter(::check)
    }

    fun defaultForLong(annos: List<Annotation>) = shrinker { i: Long ->
        val range = annos.firstInstanceOf<InRange>()
        fun check(value: Long) = range == null || value in range
        fun adjust(value: Int) = value.toLong() // this is here only to simplify copy-pasting these (yes, really)
        fun adjust(value: Long) = value

        sequence {
            if(i == adjust(0)) return@sequence
            yield(adjust(0))

            if(i == adjust(1)) return@sequence
            yield(adjust(1))

            if(range != null && i != adjust(range.from)) yield(adjust(range.from))

            if(i < 0) yield(abs(i))

            yield(adjust(i / 2))
            yield(adjust(i / 2 + 1))
        }.filter(::check)
    }

    fun defaultForShort(annos: List<Annotation>) = shrinker { i: Short ->
        val range = annos.firstInstanceOf<InRange>()
        fun check(value: Short) = range == null || value in range
        fun adjust(value: Int) = value.toShort() // this is here only to simplify copy-pasting these (yes, really)
        fun adjust(value: Long) = value.toShort()

        sequence {
            if(i == adjust(0)) return@sequence
            yield(adjust(0))

            if(i == adjust(1)) return@sequence
            yield(adjust(1))

            if(range != null && i != adjust(range.from)) yield(adjust(range.from))

            if(i < 0) yield(abs(i))

            yield(adjust(i / 2))
            yield(adjust(i / 2 + 1))
        }.filter(::check)
    }
    fun defaultForByte(annos: List<Annotation>) = shrinker { i: Byte ->
        val range = annos.filterIsInstance<InRange>().firstOrNull()
        fun check(value: Byte) = range == null || value in range
        fun adjust(value: Int) = value.toByte() // this is here only to simplify copy-pasting these (yes, really)
        fun adjust(value: Long) = value.toByte()

        sequence {
            if(i == adjust(0)) return@sequence
            yield(adjust(0))

            if(i == adjust(1)) return@sequence
            yield(adjust(1))

            if(range != null && i != adjust(range.from)) yield(adjust(range.from))

            if(i < 0) yield(abs(i))

            yield(adjust(i / 2))
            yield(adjust(i / 2 + 1))
        }.filter(::check)
    }
    fun defaultForBoolean() = shrinker { b: Boolean ->
        sequence {
            if(b) yield(false)
        }
    }
    fun defaultForFloat() = shrinker { f: Float ->
        sequence {
            if(f != 0.0f) yield(0.0f)
            else return@sequence

            if(f != 1.0f) yield(1.0f)
            else return@sequence

            if(f < 0) yield(abs(f))

            if(f != ceil(f)) yield(ceil(f))
            if(f != floor(f)) yield(floor(f))

            yield(f / 2)
        }
    }
    fun defaultForDouble() = shrinker { d: Double ->
        sequence {
            if(d != 0.0) yield(0.0)
            else return@sequence

            if(d != 1.0) yield(1.0)
            else return@sequence

            if(d < 0) yield(abs(d))

            if(d != ceil(d)) yield(ceil(d))
            if(d != floor(d)) yield(floor(d))

            yield(d / 2)
        }
    }
    fun adjustChar(ch: Char, alphabet: List<Char>) = alphabet[ch.toInt() % alphabet.size]

    fun defaultForChar(annos: List<Annotation>) = shrinker { c: Char ->
        val charRange = annos.firstInstanceOf<InCharRange>()
        val alphabet = annos.firstInstanceOf<FromAlphabet>()?.alphabet ?: charRange?.run { (from..to).joinToString("") }

        fun check(c: Char): Boolean = alphabet == null || c in alphabet
        sequence {
            if(c != 'a') yield('a')
            else return@sequence

            if(c !in 'b'..'z') yield(adjustChar(c, ('b'..'z').toList()))
            if(c.isWhitespace() && c != ' ') yield(' ')
            if(!c.isLowerCase()) yield(c.toLowerCase())
        }.filter(::check)
    }
    fun defaultForString(annos: List<Annotation>) = shrinker { s: String ->
        val forChar = defaultForChar(annos)
        val sizeRange = annos.firstInstanceOf<SizeInRange>()
        fun check(s: String) = sizeRange == null || s.length in sizeRange
        sequence {
            if(s.isNotEmpty()) {
                yield("")
                if(s.length > 1) {
                    yield(s.substring(0, s.length / 2))
                    yield(s.substring(s.length / 2))
                    yield(s.substring(s.length / 4, s.length / 4 * 3))
                }
            } else return@sequence

            yieldAll(shrinkIterable(s.asIterable(), forChar).map { it.joinToString("") })
        }.filter(::check)
    }
    fun defaultForNullable() = shrinker { t: Any? ->
        sequence {
            if(t != null) yield(null)
        }
    }
    fun <T> defaultForList(el: Shrinker<T>, annos: List<Annotation>): Shrinker<List<T>> = run {
        val sizeRange = annos.firstInstanceOf<SizeInRange>()
        fun <T> check(s: List<T>) = sizeRange == null || s.size in sizeRange
        shrinker { l: List<T> ->
            sequence {
                if(l.isNotEmpty()) {
                    yield(listOf())
                    if(l.size > 1) {
                        yield(l.subList(0, l.size / 2))
                        yield(l.subList(l.size / 2, l.size))
                        yield(l.subList(l.size / 4, l.size / 4 * 3))
                    }
                    yieldAll(shrinkIterable(l, el))
                }
            }.filter(::check)
        }
    }
    fun <T> defaultForSet(el: Shrinker<T>, annos: List<Annotation>) = run {
        val sizeRange = annos.firstInstanceOf<SizeInRange>()
        fun <T> check(s: Set<T>) = sizeRange == null || s.size in sizeRange
        shrinker { l: Set<T> ->
            sequence {
                if(l.isNotEmpty()) {
                    yield(setOf<T>())
                    if(l.size > 1) {
                        yield(l.filterIndexedTo(mutableSetOf()) { i, _ -> i < l.size / 2 })
                        yield(l.filterIndexedTo(mutableSetOf()) { i, _ -> i >= l.size / 2 })
                        yield(l.filterIndexedTo(mutableSetOf()) { i, _ -> i >= l.size / 4 && i <= l.size * 3 / 4 })
                    }

                    yieldAll(l.map { el.shrink(it) }.transposeTo { mutableSetOf<T>() })
                }
            }.filter(::check)
        }
    }
    fun <K, V> defaultForMap(k: Shrinker<K>, v: Shrinker<V>, annos: List<Annotation>) = run {
        val sizeRange = annos.firstInstanceOf<SizeInRange>()
        fun <K, V> check(s: Map<K, V>) = sizeRange == null || s.size in sizeRange
        shrinker { m: Map<K, V> ->
            sequence {
                if(m.isNotEmpty()) {
                    yield(mapOf<K, V>())
                    if(m.size > 1) {
                        yield(m.entries.take(m.size / 2).associateByTo(mutableMapOf(), { it.key }, { it.value }))
                        yield(m.entries.drop(m.size / 2).associateByTo(mutableMapOf(), { it.key }, { it.value }))
                        yield(m.entries.drop(m.size / 4).take(m.size / 2).associateByTo(mutableMapOf(), { it.key }, { it.value }))
                    }
                    yieldAll(m.map { k.shrink(it.key) zip v.shrink(it.value) }.transpose().map { it.toMap() })
                }
            }.filter(::check)
        }
    }
    fun <A, B> defaultForPair(a: Shrinker<A>, b: Shrinker<B>) = run {
        shrinker { p: Pair<A, B> ->
            sequence {
                val firstSeq = a.shrink(p.first)
                val secondSeq = b.shrink(p.second)
                yieldAll(firstSeq zip secondSeq)
                yieldAll(firstSeq.map { it to p.second })
                yieldAll(secondSeq.map { p.first to it })
            }.distinct()
        }
    }
    fun <A, B, C> defaultForTriple(a: Shrinker<A>, b: Shrinker<B>, c: Shrinker<C>) = run {
        shrinker { t: Triple<A, B, C> ->
            val ash = a.shrink(t.first).iterator()
            val bsh = b.shrink(t.second).iterator()
            val csh = c.shrink(t.third).iterator()
            sequence {
                while(ash.hasNext() && bsh.hasNext() && csh.hasNext()) {
                    yield(Triple(ash.next(), bsh.next(), csh.next()))
                }
            }
        }
    }

    fun exportDefaults() {
        with(TypeClasses) {
            val shrinker = Shrinker::class
            instance { -> defaultForNullable() as Shrinker<Any?> }
            instance { -> defaultForBoolean() }
            instance { -> defaultForDouble() }
            instance { -> defaultForFloat() }
            instance { _, annos -> defaultForByte(annos) }
            instance { _, annos -> defaultForShort(annos) }
            instance { _, annos -> defaultForInt(annos) }
            instance { _, annos -> defaultForLong(annos) }
            instance { _, annos -> defaultForChar(annos) }
            instance { _, annos -> defaultForString(annos) }

            instance { (arg), annos -> defaultForList(shrinker of arg, annos) as Shrinker<List<*>> }
            instance { (arg), annos -> defaultForSet(shrinker of arg, annos) as Shrinker<Set<*>> }
            instance { (arg1, arg2), annos -> defaultForMap(shrinker of arg1, shrinker of arg2, annos) as Shrinker<Map<*, *>> }

            instance { (arg1, arg2) -> defaultForPair(
                    shrinker of arg1,
                    shrinker of arg2
            ) as Shrinker<Pair<*,*>> }
            instance { (arg1, arg2, arg3) -> defaultForTriple(
                    shrinker of arg1,
                    shrinker of arg2,
                    shrinker of arg3
            ) as Shrinker<Triple<*,*,*>> }

            instance { _, annos -> defaultForString(annos) as Shrinker<CharSequence> }
            instance { (arg), annos -> defaultForList(shrinker of arg, annos) as Shrinker<Collection<*>> }
            instance { (arg), annos -> defaultForList(shrinker of arg, annos) as Shrinker<Iterable<*>> }

            instance { elements ->
                check(elements.size == 1)
                val als = elements.map { shrinker of it } as List<Shrinker<Any?>>
                shrinker { t: Tuple1<*> ->
                    shrinkMany(t.toList(), als).map { Tuple.ofList(it) as Tuple1<Any?> }
                }
            }
            instance { elements ->
                check(elements.size == 2)
                val als = elements.map { shrinker of it } as List<Shrinker<Any?>>
                shrinker { t: Tuple2<*, *> ->
                    shrinkMany(t.toList(), als.toList()).map { Tuple.ofList(it) as Tuple2<Any?, Any?> }
                }
            }
            instance { elements ->
                check(elements.size == 3)
                val als = elements.map { shrinker of it } as List<Shrinker<Any?>>
                shrinker { t: Tuple3<*, *, *> ->
                    shrinkMany(t.toList(), als.toList()).map { Tuple.ofList(it) as Tuple3<Any?, Any?, Any?> }
                }
            }
            instance { elements ->
                check(elements.size == 4)

                val als = elements.map { shrinker of it } as List<Shrinker<Any?>>
                shrinker { t: Tuple4<*, *, *, *> ->
                    shrinkMany(t.toList(), als.toList()).map { Tuple.ofList(it) as Tuple4<Any?, Any?, Any?, Any?> }
                }
            }
            instance { elements ->
                check(elements.size == 5)
                val als = elements.map { shrinker of it } as List<Shrinker<Any?>>
                shrinker { t: Tuple5<*, *, *, *, *> ->
                    shrinkMany(t.toList(), als.toList()).map { Tuple.ofList(it) as Tuple5<Any?, Any?, Any?, Any?, Any?> }
                }
            }
            instance { elements ->
                check(elements.size == 6)
                val als = elements.map { shrinker of it } as List<Shrinker<Any?>>
                shrinker { t: Tuple6<*, *, *, *, *, *> ->
                    shrinkMany(t.toList(), als.toList()).map { Tuple.ofList(it) as Tuple6<Any?, Any?, Any?, Any?, Any?, Any?> }
                }
            }
            instance { elements ->
                check(elements.size == 7)
                val als = elements.map { shrinker of it } as List<Shrinker<Any?>>
                shrinker { t: Tuple7<*, *, *, *, *, *, *> ->
                    shrinkMany(t.toList(), als.toList()).map { Tuple.ofList(it) as Tuple7<Any?, Any?, Any?, Any?, Any?, Any?, Any?> }
                }
            }


        }
    }

}
