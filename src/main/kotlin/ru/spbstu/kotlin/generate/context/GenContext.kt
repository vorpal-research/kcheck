package ru.spbstu.kotlin.generate.context

import com.sun.org.apache.xpath.internal.operations.Bool
import ru.spbstu.kotlin.generate.cases.*
import ru.spbstu.kotlin.generate.combinators.*
import ru.spbstu.kotlin.generate.util.FancyFunctions
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

    fun getGen(type: TypeHolder): Gen<*>? {
        when(type.arguments.size) {
            0 -> return default[type]
            1 -> {
                val argGen = getGen(type.arguments.first())
                argGen ?: return null
                return generic1[type.copy(arguments = emptyList())]?.invoke(argGen)
            }
            2 -> {
                val argGen0 = getGen(type.arguments[0])
                argGen0 ?: return null
                val argGen1 = getGen(type.arguments[1])
                argGen1 ?: return null
                return generic2[type.copy(arguments = emptyList())]?.invoke(argGen0, argGen1)
            }
            3 -> {
                val argGen0 = getGen(type.arguments[0])
                argGen0 ?: return null
                val argGen1 = getGen(type.arguments[1])
                argGen1 ?: return null
                val argGen2 = getGen(type.arguments[2])
                argGen2 ?: return null
                return generic3[type.copy(arguments = emptyList())]?.invoke(argGen0, argGen1, argGen2)
            }
            4 -> {
                val argGen0 = getGen(type.arguments[0])
                argGen0 ?: return null
                val argGen1 = getGen(type.arguments[1])
                argGen1 ?: return null
                val argGen2 = getGen(type.arguments[2])
                argGen2 ?: return null
                val argGen3 = getGen(type.arguments[3])
                argGen3 ?: return null
                return generic4[type.copy(arguments = emptyList())]?.invoke(argGen0, argGen1, argGen2, argGen3)
            }
            else -> return TODO()
        }
    }

    fun setDefault(th: TypeHolder, gen: Gen<*>) {
        default[th] = gen
        if(!th.isNullable) {
            default[th.copy(isNullable = true)] = anyNullable(gen)
        }
    }

    fun setDefault(th: TypeHolder, gen: (Gen<*>) -> Gen<*>) {
        val type = th.copy(arguments = emptyList())
        generic1[type] = gen
        if(!type.isNullable) {
            generic1[type.copy(isNullable = true)] = with(FancyFunctions){ gen.mapResult{ anyNullable(it) } }
        }
    }

    fun setDefault(th: TypeHolder, gen: (Gen<*>, Gen<*>) -> Gen<*>) {
        val type = th.copy(arguments = emptyList())
        generic2[type] = gen
        if(!type.isNullable) {
            generic2[type.copy(isNullable = true)] = with(FancyFunctions){ gen.mapResult{ anyNullable(it) } }
        }
    }

    fun setDefault(th: TypeHolder, gen: (Gen<*>, Gen<*>, Gen<*>) -> Gen<*>) {
        val type = th.copy(arguments = emptyList())
        generic3[type] = gen
        if(!type.isNullable) {
            generic3[type.copy(isNullable = true)] = with(FancyFunctions){ gen.mapResult{ anyNullable(it) } }
        }
    }

    fun setDefault(th: TypeHolder, gen: (Gen<*>, Gen<*>, Gen<*>, Gen<*>) -> Gen<*>) {
        val type = th.copy(arguments = emptyList())
        generic4[type] = gen
        if(!type.isNullable) {
            generic4[type.copy(isNullable = true)] = with(FancyFunctions){ gen.mapResult{ anyNullable(it) } }
        }
    }


    @JvmName("installGenerator")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: () -> G) {
        val type = buildTH<G>(function).arguments.first()
        val gen = function()
        setDefault(type, gen)
    }

    @JvmName("installGenericGenerator1")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: (Gen<*>) -> G) {
        val type = buildTH<G>(function.reflect()?.returnType!!).arguments.first()
        setDefault(type, function)
    }

    @JvmName("installGenericGenerator2")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: (Gen<*>, Gen<*>) -> G) {
        val type = buildTH<G>(function.reflect()?.returnType!!).arguments.first()
        setDefault(type, function)
    }

    @JvmName("installGenericGenerator3")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: (Gen<*>, Gen<*>, Gen<*>) -> G) {
        val type = buildTH<G>(function.reflect()?.returnType!!).arguments.first()
        setDefault(type, function)
    }

    @JvmName("installGenericGenerator3")
    inline fun <reified T, reified G : Gen<T>> install(noinline function: (Gen<*>, Gen<*>, Gen<*>, Gen<*>) -> G) {
        val type = buildTH<G>(function.reflect()?.returnType!!).arguments.first()
        setDefault(type, function)
    }

    inline fun <reified T> installFunction(noinline function: () -> T) {
        val type = buildTH<T>(function)
        val gen = gen(function)
        setDefault(type, gen)
    }

    inline fun <reified T1, reified T> installFunction(noinline function: (T1) -> T) {
        val ref = function.reflect()
        val type = buildTH<T>(ref?.returnType!!)
        val type1 = buildTH<T1>(ref?.parameters?.get(0)?.type!!)

        val gen = gen{
            val argGen = getGen(type1) as? Gen<T1>
            argGen?.nextValue()?.let(function)!!
        }
        setDefault(type, gen)
    }

    inline fun <reified T1, reified T2, reified T> installFunction(noinline function: (T1, T2) -> T) {
        val ref = function.reflect()
        val type = buildTH<T>(ref?.returnType!!)
        val type1 = buildTH<T1>(ref?.parameters?.get(0)?.type!!)
        val type2 = buildTH<T2>(ref?.parameters?.get(1)?.type!!)

        val gen = gen{
            val arg1Gen = getGen(type1) as Gen<T1>
            val arg2Gen = getGen(type2) as Gen<T2>
            function(arg1Gen.nextValue(), arg2Gen.nextValue())
        }
        setDefault(type, gen)
    }

    inline fun <reified T1, reified T2, reified T3, reified T> installFunction(noinline function: (T1, T2, T3) -> T) {
        val ref = function.reflect()
        val type = buildTH<T>(ref?.returnType!!)
        val type1 = buildTH<T1>(ref?.parameters?.get(0)?.type!!)
        val type2 = buildTH<T2>(ref?.parameters?.get(1)?.type!!)
        val type3 = buildTH<T3>(ref?.parameters?.get(2)?.type!!)

        val gen = gen{
            val arg1Gen = getGen(type1) as Gen<T1>
            val arg2Gen = getGen(type2) as Gen<T2>
            val arg3Gen = getGen(type3) as Gen<T3>
            function(arg1Gen.nextValue(), arg2Gen.nextValue(), arg3Gen.nextValue())
        }
        setDefault(type, gen)
    }

    inline fun <reified T1, reified T2, reified T3, reified T4, reified T> installFunction(noinline function: (T1, T2, T3, T4) -> T) {
        val ref = function.reflect()
        val type = buildTH<T>(ref?.returnType!!)
        val type1 = buildTH<T1>(ref?.parameters?.get(0)?.type!!)
        val type2 = buildTH<T2>(ref?.parameters?.get(1)?.type!!)
        val type3 = buildTH<T3>(ref?.parameters?.get(2)?.type!!)
        val type4 = buildTH<T4>(ref?.parameters?.get(3)?.type!!)

        val gen = gen{
            val arg1Gen = getGen(type1) as Gen<T1>
            val arg2Gen = getGen(type2) as Gen<T2>
            val arg3Gen = getGen(type3) as Gen<T3>
            val arg4Gen = getGen(type4) as Gen<T4>
            function(arg1Gen.nextValue(), arg2Gen.nextValue(), arg3Gen.nextValue(), arg4Gen.nextValue())
        }
        setDefault(type, gen)
    }


    inline fun <reified T, R> feed(noinline function: (T) -> R): R {
        val type = buildTH<T>(function.reflect()?.parameters?.first()?.type!!)
        return function(getGen(type)?.nextValue() as T)
    }

    inline fun <reified T> forAll(tries: Int = 100, noinline function: (T) -> Boolean) =
            (0..tries).fold(true) { acc, v -> acc and feed(function) }

    inline fun <reified T1, reified T2, R> feed(noinline function: (T1, T2) -> R): R {
        val arg1 = buildTH<T1>(function.reflect()?.parameters?.get(0)?.type!!)
        val arg2 = buildTH<T2>(function.reflect()?.parameters?.get(1)?.type!!)
        return function(
                getGen(arg1)?.nextValue() as T1,
                getGen(arg2)?.nextValue() as T2
        )
    }

    inline fun <reified T1, reified T2> forAll(tries: Int = 100, noinline function: (T1, T2) -> Boolean) =
            (0..tries).fold(true) { acc, v -> acc and feed(function) }

    inline fun <reified T1, reified T2, reified T3, R> feed(noinline function: (T1, T2, T3) -> R): R {
        val arg1 = buildTH<T1>(function.reflect()?.parameters?.get(0)?.type!!)
        val arg2 = buildTH<T2>(function.reflect()?.parameters?.get(1)?.type!!)
        val arg3 = buildTH<T3>(function.reflect()?.parameters?.get(2)?.type!!)
        return function(
                getGen(arg1)?.nextValue() as T1,
                getGen(arg2)?.nextValue() as T2,
                getGen(arg3)?.nextValue() as T3
        )
    }

    inline fun <reified T1, reified T2, reified T3> forAll(tries: Int = 100, noinline function: (T1, T2, T3) -> Boolean) =
            (0..tries).fold(true) { acc, v -> acc and feed(function) }

    inline fun <reified T1, reified T2, reified T3, reified T4, R> feed(noinline function: (T1, T2, T3, T4) -> R): R {
        val arg1 = buildTH<T1>(function.reflect()?.parameters?.get(0)?.type!!)
        val arg2 = buildTH<T2>(function.reflect()?.parameters?.get(1)?.type!!)
        val arg3 = buildTH<T3>(function.reflect()?.parameters?.get(2)?.type!!)
        val arg4 = buildTH<T3>(function.reflect()?.parameters?.get(3)?.type!!)
        return function(
                getGen(arg1)?.nextValue() as T1,
                getGen(arg2)?.nextValue() as T2,
                getGen(arg3)?.nextValue() as T3,
                getGen(arg4)?.nextValue() as T4
        )
    }

    inline fun <reified T1, reified T2, reified T3, reified T4> forAll(tries: Int = 100, noinline function: (T1, T2, T3, T4) -> Boolean) =
            (0..tries).fold(true) { acc, v -> acc and feed(function) }

}

