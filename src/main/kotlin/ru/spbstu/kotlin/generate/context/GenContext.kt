@file:Suppress("UNCHECKED_CAST")
package ru.spbstu.kotlin.generate.context

import ru.spbstu.kotlin.generate.assume.AssumptionFailedException
import ru.spbstu.kotlin.generate.cases.anyArray
import ru.spbstu.kotlin.generate.cases.anyNullable
import ru.spbstu.kotlin.generate.combinators.Gen
import ru.spbstu.kotlin.generate.combinators.gen
import ru.spbstu.kotlin.reflection.quasi.buildTypeHolder
import ru.spbstu.kotlin.reflection.quasi.typeOf
import java.util.*
import kotlin.reflect.jvm.reflect

abstract class GenContext(val random: Random = Random()): TypeClassContext<Gen<*>>() {

    override fun handleNullable(tc: Gen<*>): Gen<*> = anyNullable(tc)
    override fun handleArray(element: Class<*>, elementTC: Gen<*>) =
            anyArray(
                    @Suppress("UNCHECKED_CAST")(element as Class<Any>),
                    @Suppress("UNCHECKED_CAST")(elementTC as Gen<Any>)
            )

    @JvmName("installGenerator")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: () -> G) {
        val type = typeOf(function).arguments.first()
        val gen = function()
        set(type, gen)
    }

    @JvmName("installGenericGenerator1")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: (Gen<*>) -> G) {
        val type = buildTypeHolder<G>(function.reflect()?.returnType!!).arguments.first()
        set(type, function)
    }

    @JvmName("installGenericGenerator2")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: (Gen<*>, Gen<*>) -> G) {
        val type = buildTypeHolder<G>(function.reflect()?.returnType!!).arguments.first()
        set(type, function)
    }

    @JvmName("installGenericGenerator3")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: (Gen<*>, Gen<*>, Gen<*>) -> G) {
        val type = buildTypeHolder<G>(function.reflect()?.returnType!!).arguments.first()
        set(type, function)
    }

    @JvmName("installGenericGenerator3")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: (Gen<*>, Gen<*>, Gen<*>, Gen<*>) -> G) {
        val type = buildTypeHolder<G>(function.reflect()?.returnType!!).arguments.first()
        set(type, function)
    }

    inline fun <reified T> installFunction(noinline function: () -> T) {
        val type = typeOf(function)
        val gen = gen(function)
        set(type, gen)
    }

    inline fun <reified T1, reified T> installFunction(noinline function: (T1) -> T) {
        val ref = function.reflect()
        val type = buildTypeHolder<T>(ref?.returnType!!)
        val type1 = buildTypeHolder<T1>(ref?.parameters?.get(0)?.type!!)

        val gen = gen{
            val argGen = get(type1) as? Gen<T1>
            argGen?.nextValue()?.let(function)!!
        }
        set(type, gen)
    }

    inline fun <reified T1, reified T2, reified T> installFunction(noinline function: (T1, T2) -> T) {
        val ref = function.reflect()
        val type = buildTypeHolder<T>(ref?.returnType!!)
        val type1 = buildTypeHolder<T1>(ref?.parameters?.get(0)?.type!!)
        val type2 = buildTypeHolder<T2>(ref?.parameters?.get(1)?.type!!)

        println(type)
        println(type1)
        println(type2)

        val gen = gen{
            val arg1Gen = get(type1) as Gen<T1>
            val arg2Gen = get(type2) as Gen<T2>
            function(arg1Gen.nextValue(), arg2Gen.nextValue())
        }
        set(type, gen)
    }

    inline fun <reified T1, reified T2, reified T3, reified T> installFunction(noinline function: (T1, T2, T3) -> T) {
        val ref = function.reflect()
        val type = buildTypeHolder<T>(ref?.returnType!!)
        val type1 = buildTypeHolder<T1>(ref?.parameters?.get(0)?.type!!)
        val type2 = buildTypeHolder<T2>(ref?.parameters?.get(1)?.type!!)
        val type3 = buildTypeHolder<T3>(ref?.parameters?.get(2)?.type!!)

        val gen = gen{
            val arg1Gen = get(type1) as Gen<T1>
            val arg2Gen = get(type2) as Gen<T2>
            val arg3Gen = get(type3) as Gen<T3>
            function(arg1Gen.nextValue(), arg2Gen.nextValue(), arg3Gen.nextValue())
        }
        set(type, gen)
    }

    inline fun <reified T1, reified T2, reified T3, reified T4, reified T> installFunction(noinline function: (T1, T2, T3, T4) -> T) {
        val ref = function.reflect()
        val type = buildTypeHolder<T>(ref?.returnType!!)
        val type1 = buildTypeHolder<T1>(ref?.parameters?.get(0)?.type!!)
        val type2 = buildTypeHolder<T2>(ref?.parameters?.get(1)?.type!!)
        val type3 = buildTypeHolder<T3>(ref?.parameters?.get(2)?.type!!)
        val type4 = buildTypeHolder<T4>(ref?.parameters?.get(3)?.type!!)

        val gen = gen{
            val arg1Gen = get(type1) as Gen<T1>
            val arg2Gen = get(type2) as Gen<T2>
            val arg3Gen = get(type3) as Gen<T3>
            val arg4Gen = get(type4) as Gen<T4>
            function(arg1Gen.nextValue(), arg2Gen.nextValue(), arg3Gen.nextValue(), arg4Gen.nextValue())
        }
        set(type, gen)
    }

    inline fun<R> retrying(limit: Int = 100, body: () -> R): R {
        var count = limit
        while(--count != 0) {
            try{ return body() }
            catch (retry: AssumptionFailedException) {
                continue
            }
        }
        throw AssumptionFailedException()
    }

    inline fun <reified T, R> feed(noinline function: (T) -> R): R {
        val type = buildTypeHolder<T>(function.reflect()?.parameters?.first()?.type!!)
        return retrying { function(get(type)?.nextValue() as T) }
    }

    inline fun <reified T> forAll(tries: Int = 100, noinline function: (T) -> Boolean) =
            (0..tries - 1).fold(true) { acc, v -> acc and feed(function) }

    inline fun <reified T1, reified T2, R> feed(noinline function: (T1, T2) -> R): R {
        val arg1 = buildTypeHolder<T1>(function.reflect()?.parameters?.get(0)?.type!!)
        val arg2 = buildTypeHolder<T2>(function.reflect()?.parameters?.get(1)?.type!!)
        return retrying {
            function(
                get(arg1)?.nextValue() as T1,
                get(arg2)?.nextValue() as T2
            )
        }
    }

    inline fun <reified T1, reified T2> forAll(tries: Int = 100, noinline function: (T1, T2) -> Boolean) =
            (0..tries - 1).fold(true) { acc, v -> acc and feed(function) }

    inline fun <reified T1, reified T2, reified T3, R> feed(noinline function: (T1, T2, T3) -> R): R {
        val arg1 = buildTypeHolder<T1>(function.reflect()?.parameters?.get(0)?.type!!)
        val arg2 = buildTypeHolder<T2>(function.reflect()?.parameters?.get(1)?.type!!)
        val arg3 = buildTypeHolder<T3>(function.reflect()?.parameters?.get(2)?.type!!)
        return retrying {
            function(
                    get(arg1)?.nextValue() as T1,
                    get(arg2)?.nextValue() as T2,
                    get(arg3)?.nextValue() as T3
            )
        }
    }

    inline fun <reified T1, reified T2, reified T3> forAll(tries: Int = 100, noinline function: (T1, T2, T3) -> Boolean) =
            (0..tries - 1).fold(true) { acc, v -> acc and feed(function) }

    inline fun <reified T1, reified T2, reified T3, reified T4, R> feed(noinline function: (T1, T2, T3, T4) -> R): R {
        val arg1 = buildTypeHolder<T1>(function.reflect()?.parameters?.get(0)?.type!!)
        val arg2 = buildTypeHolder<T2>(function.reflect()?.parameters?.get(1)?.type!!)
        val arg3 = buildTypeHolder<T3>(function.reflect()?.parameters?.get(2)?.type!!)
        val arg4 = buildTypeHolder<T4>(function.reflect()?.parameters?.get(3)?.type!!)
        return retrying {
            function(
                    get(arg1)?.nextValue() as T1,
                    get(arg2)?.nextValue() as T2,
                    get(arg3)?.nextValue() as T3,
                    get(arg4)?.nextValue() as T4
            )
        }
    }

    inline fun <reified T1, reified T2, reified T3, reified T4> forAll(tries: Int = 100, noinline function: (T1, T2, T3, T4) -> Boolean) =
            (0..tries - 1).fold(true) { acc, v -> acc and feed(function) }

}

