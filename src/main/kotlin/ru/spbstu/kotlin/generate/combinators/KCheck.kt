package ru.spbstu.kotlin.generate.combinators

import ru.spbstu.kotlin.generate.assume.AssumptionFailedException
import ru.spbstu.kotlin.typeclass.TypeClasses
import ru.spbstu.ktuples.*
import ru.spbstu.wheels.getOrElse
import ru.spbstu.wheels.tryEx
import java.util.concurrent.TimeoutException
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.reflect

data class ForInputException(val data: Any?, val nested: Exception) :
        Exception("forAll failed for input data: $data", nested)

inline fun<R> retrying(limit: Int = 50, appendable: Appendable = NullAppendable, body: () -> R): R {
    var count = limit
    while(--count != 0) {
        try{ return body() }
        catch (retry: AssumptionFailedException) {
            appendable.appendln("Assumption failed, retrying")
            continue
        }
    }
    throw AssumptionFailedException()
}

@PublishedApi
internal fun <T> generate(
        iterations: Int,
        gen: Arbitrary<T>,
        appendable: Appendable,
        body: (T) -> Unit
): Pair<T, Exception>? = with(appendable) {
    appendln("Generating...")
    repeat(iterations) {
        retrying(appendable = appendable) {
            val value = gen.next()
            appendln("Generated: $value")
            try {
                body(value)
            } catch (assumption: AssumptionFailedException) {
                throw assumption
            } catch (ex: Exception) {
                return value to ex
            }
        }
    }
    return null
}

@PublishedApi
internal fun <T> shrink(
        iterations: Int,
        shrinker: Shrinker<T>,
        value: T,
        ex: Exception,
        appendable: Appendable,
        body: (T) -> Unit
): Pair<T, Exception> = with(appendable) {
    appendln("Shrinking $value...")
    var current = value
    var currentEx: Exception = ex
    val tried = mutableSetOf<T>()
    repeat(iterations) {
        val nexts = shrinker.shrink(current)
        for(v in nexts) {
            if(v in tried) continue

            tried.add(v)
            appendln("Trying $v")
            try {
                body(v)
            }
            catch(assume: AssumptionFailedException) {}
            // do not shrink on timeout
            catch(timeout: TimeoutException) { return@with v to timeout }
            catch(ex: Exception) {
                current = v
                currentEx = ex
                break
            }
        }
    }
    return@with current to currentEx
}.also { appendable.appendln("Shrinking result: $it") }

object NullAppendable : Appendable {
    override fun append(csq: CharSequence?): Appendable = this
    override fun append(csq: CharSequence?, start: Int, end: Int): Appendable = this
    override fun append(c: Char): Appendable = this
}

object NoShrinker : Shrinker<Any?>

object KCheck {

    init {
        Arbitrary.exportDefaults()
        Shrinker.exportDefaults()
    }

    val arbitrary = Arbitrary::class
    val shrinker = Shrinker::class

    fun <T> getShrinker(type: KType) = tryEx { TypeClasses.get<Shrinker<*>>(type) as Shrinker<T> }
            .getOrElse { NoShrinker as Shrinker<T> }

    inline fun <reified T> forAll(generationIterations: Int = 100,
                                  shrinkingIterations: Int = 100,
                                  appendable: Appendable = NullAppendable,
                                  noinline body: (T) -> Unit) {
        val type = body.reflect()!!.parameters.first().type
        val generator = TypeClasses.get<Arbitrary<*>>(type) as Arbitrary<T>
        val shrinker = getShrinker<T>(type)

        val (generated, ex) = generate(generationIterations, generator, appendable, body) ?: return
        val (shrinked, ex2) = shrink(shrinkingIterations, shrinker, generated, ex, appendable, body)
        throw ForInputException(shrinked, ex2)
    }

    inline fun <reified A, reified B> forAll(generationIterations: Int = 100,
                                  shrinkingIterations: Int = 100,
                                  appendable: Appendable = NullAppendable,
                                  noinline body: (A, B) -> Unit) {
        with(TypeClasses) {
            val types = body.reflect()!!.parameters.map { it.type }
            val tuple = Tuple2::class.createType(types.map { KTypeProjection.invariant(it) })
            val generator = (arbitrary of tuple) as Arbitrary<Tuple2<A, B>>
            val shrinker = getShrinker<Tuple2<A, B>>(tuple)

            val tupleBody = { t: Tuple2<A, B> -> t.letAll(body) }

            val (generated, ex) =
                    generate(generationIterations, generator, appendable, tupleBody) ?: return
            val (shrinked, ex2) = shrink(shrinkingIterations, shrinker, generated, ex, appendable, tupleBody)
            throw ForInputException(shrinked, ex2)
        }
    }

    inline fun <reified A, reified B, reified C> forAll(generationIterations: Int = 100,
                                             shrinkingIterations: Int = 100,
                                             appendable: Appendable = NullAppendable,
                                             noinline body: (A, B, C) -> Unit) {
        with(TypeClasses) {
            val types = body.reflect()!!.parameters.map { it.type }
            val tuple = Tuple3::class.createType(types.map { KTypeProjection.invariant(it) })
            val generator = (arbitrary of tuple) as Arbitrary<Tuple3<A, B, C>>
            val shrinker = getShrinker<Tuple3<A, B, C>>(tuple)

            val tupleBody = { t: Tuple3<A, B, C> -> t.letAll(body) }

            val (generated, ex) =
                    generate(generationIterations, generator, appendable, tupleBody) ?: return
            val (shrinked, ex2) = shrink(shrinkingIterations, shrinker, generated, ex, appendable, tupleBody)
            throw ForInputException(shrinked, ex2)
        }
    }

    inline fun <reified A, reified B, reified C, reified D> forAll(generationIterations: Int = 100,
                                             shrinkingIterations: Int = 100,
                                             appendable: Appendable = NullAppendable,
                                             noinline body: (A, B, C, D) -> Unit) {
        with(TypeClasses) {
            val types = body.reflect()!!.parameters.map { it.type }
            val tuple = Tuple4::class.createType(types.map { KTypeProjection.invariant(it) })
            val generator = (arbitrary of tuple) as Arbitrary<Tuple4<A, B, C, D>>
            val shrinker = getShrinker<Tuple4<A, B, C, D>>(tuple)

            val tupleBody = { t: Tuple4<A, B, C, D> -> t.letAll(body) }

            val (generated, ex) =
                    generate(generationIterations, generator, appendable, tupleBody) ?: return
            val (shrinked, ex2) = shrink(shrinkingIterations, shrinker, generated, ex, appendable, tupleBody)
            throw ForInputException(shrinked, ex2)
        }
    }

    inline fun <reified A, reified B, reified C,
            reified D, reified E> forAll(generationIterations: Int = 100,
                                             shrinkingIterations: Int = 100,
                                             appendable: Appendable = NullAppendable,
                                             noinline body: (A, B, C, D, E) -> Unit) {
        with(TypeClasses) {
            val types = body.reflect()!!.parameters.map { it.type }
            val tuple = Tuple5::class.createType(types.map { KTypeProjection.invariant(it) })
            val generator = (arbitrary of tuple) as Arbitrary<Tuple5<A, B, C, D, E>>
            val shrinker = getShrinker<Tuple5<A, B, C, D, E>>(tuple)

            val tupleBody = { t: Tuple5<A, B, C, D, E> -> t.letAll(body) }

            val (generated, ex) =
                    generate(generationIterations, generator, appendable, tupleBody) ?: return
            val (shrinked, ex2) = shrink(shrinkingIterations, shrinker, generated, ex, appendable, tupleBody)
            throw ForInputException(shrinked, ex2)
        }
    }

    inline fun <reified A, reified B, reified C,
            reified D, reified E, reified F> forAll(generationIterations: Int = 100,
                                         shrinkingIterations: Int = 100,
                                         appendable: Appendable = NullAppendable,
                                         noinline body: (A, B, C, D, E, F) -> Unit) {
        with(TypeClasses) {
            val types = body.reflect()!!.parameters.map { it.type }
            val tuple = Tuple5::class.createType(types.map { KTypeProjection.invariant(it) })
            val generator = (arbitrary of tuple) as Arbitrary<Tuple6<A, B, C, D, E, F>>
            val shrinker = getShrinker<Tuple6<A, B, C, D, E, F>>(tuple)

            val tupleBody = { t: Tuple6<A, B, C, D, E, F> -> t.letAll(body) }

            val (generated, ex) =
                    generate(generationIterations, generator, appendable, tupleBody) ?: return
            val (shrinked, ex2) = shrink(shrinkingIterations, shrinker, generated, ex, appendable, tupleBody)
            throw ForInputException(shrinked, ex2)
        }
    }

}
