package ru.spbstu.kotlin.generate.cases

import ru.spbstu.kotlin.generate.context.GenContext
import ru.spbstu.kotlin.generate.combinators.Gen
import ru.spbstu.kotlin.generate.combinators.gen
import ru.spbstu.kotlin.generate.combinators.map
import java.math.BigInteger

fun GenContext.anyString(cGen: Gen<Char>, maxLen: Int) =
        gen{ IntRange(0, random.nextInt(maxLen)).map { cGen.nextValue() }.joinToString("") }

fun GenContext.anyReadableString() =
        priorities(
                1 to constant(""),
                1 to anyString(anyCommonChar(), 1),
                3 to anyString(anyCommonChar(), 20),
                3 to anyString(anyCommonChar(), 255)
        )

fun GenContext.anyString() =
        priorities(
                1 to constant(""),
                1 to anyString(anyChar(), 1),
                3 to anyString(anyChar(), 20),
                3 to anyString(anyChar(), 255)
        )

fun <T> GenContext.anyList(elGen: Gen<T>, maxLen: Int): Gen<List<T>> =
        gen{ IntRange(0, random.nextInt(maxLen)).map{ elGen.nextValue() } }

fun <T> GenContext.anyList(elGen: Gen<T>): Gen<List<T>> =
        priorities(
                1 to constant(emptyList<T>()),
                1 to gen { listOf(elGen.nextValue()) },
                3 to anyList(elGen, 20),
                3 to anyList(elGen, 255),
                1 to anyList(elGen, 5000)
        )

fun <T> GenContext.anySet(elGen: Gen<T>): Gen<Set<T>> =
        anyList(elGen).map { it.toSet() }

fun <K, V> GenContext.anyMap(kGen: Gen<K>, vGen: Gen<V>): Gen<Map<K, V>> =
        anyList(anyPair(kGen, vGen)).map{ it.toMap() }

fun <A, B> GenContext.anyPair(aGen: Gen<A>, bGen: Gen<B>): Gen<Pair<A, B>> =
        gen { aGen.nextValue() to bGen.nextValue() }

fun <A, B, C> GenContext.anyTriple(aGen: Gen<A>, bGen: Gen<B>, cGen: Gen<C>): Gen<Triple<A, B, C>> =
        gen { Triple(aGen.nextValue(), bGen.nextValue(), cGen.nextValue()) }

fun GenContext.anyBigInt(): Gen<BigInteger> = gen{
    val byteSize = random.nextInt(32)
    val bytes = ByteArray(byteSize + 1)
    random.nextBytes(bytes)
    BigInteger(bytes)
}

