package ru.spbstu.kotlin.generate.arbitraries

import ru.spbstu.kotlin.generate.combinators.*
import java.math.BigInteger

fun GenerationContext.anyString(cGen: Arbitrary<Char>, maxLen: Int) =
        arbitrary { IntRange(0, random.nextInt(maxLen)).map { cGen.next() }.joinToString("") }

fun GenerationContext.anyString(charGen: Arbitrary<Char>, sizeGen: Arbitrary<Int>) =
        arbitrary { (0..sizeGen.next()).map { charGen.next() }.joinToString("") }

fun GenerationContext.anyReadableString() =
        priorities(
                1 to constant(""),
                1 to anyString(anyCommonChar(), 1),
                3 to anyString(anyCommonChar(), 20),
                3 to anyString(anyCommonChar(), 255)
        )

fun GenerationContext.anyString() =
        priorities(
                1 to constant(""),
                1 to anyString(anyChar(), 1),
                3 to anyString(anyChar(), 20),
                3 to anyString(anyChar(), 255)
        )

fun <T> GenerationContext.anyList(elGen: Arbitrary<T>, maxLen: Int): Arbitrary<List<T>> =
        arbitrary { IntRange(0, random.nextInt(maxLen)).map { elGen.next() } }

fun <T> GenerationContext.anyList(elGen: Arbitrary<T>, sizeGen: Arbitrary<Int>): Arbitrary<List<T>> =
        arbitrary { IntRange(0, sizeGen.next()).map { elGen.next() } }

fun <T> GenerationContext.anySet(elGen: Arbitrary<T>, sizeGen: Arbitrary<Int>): Arbitrary<Set<T>> =
        anyList(elGen, sizeGen).map { it.toSet() }

fun <K, V> GenerationContext.anyMap(kGen: Arbitrary<K>, vGen: Arbitrary<V>, sizeGen: Arbitrary<Int>): Arbitrary<Map<K, V>> =
        anyList(anyPair(kGen, vGen), sizeGen).map { it.toMap() }

fun <A, B> GenerationContext.anyPair(aGen: Arbitrary<A>, bGen: Arbitrary<B>): Arbitrary<Pair<A, B>> =
        arbitrary { aGen.next() to bGen.next() }

fun <A, B, C> GenerationContext.anyTriple(aGen: Arbitrary<A>, bGen: Arbitrary<B>, cGen: Arbitrary<C>): Arbitrary<Triple<A, B, C>> =
        arbitrary { Triple(aGen.next(), bGen.next(), cGen.next()) }

fun GenerationContext.anyBigInt(): Arbitrary<BigInteger> = arbitrary {
    val byteSize = random.nextInt(32)
    val bytes = ByteArray(byteSize + 1)
    random.nextBytes(bytes)
    BigInteger(bytes)
}

