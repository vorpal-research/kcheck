package ru.spbstu.kotlin.generate.context

import ru.spbstu.kotlin.generate.cases.anyNullable
import ru.spbstu.kotlin.generate.combinators.Gen
import ru.spbstu.kotlin.generate.util.FancyFunctions
import ru.spbstu.kotlin.generate.util.FancyFunctions.mapResult
import ru.spbstu.kotlin.reflection.quasi.Mutability
import ru.spbstu.kotlin.reflection.quasi.TypeHolder
import java.util.*

abstract class TypeClassContext<TC> {

    abstract fun handleNullable(tc: TC): TC
    abstract fun handleArray(element: Class<*>, elementTC: TC): TC

    val default = HashMap<TypeHolder, TC>()
    val generic1 = HashMap<TypeHolder, (TC) -> TC>()
    val generic2 = HashMap<TypeHolder, (TC, TC) -> TC>()
    val generic3 = HashMap<TypeHolder, (TC, TC, TC) -> TC>()
    val generic4 = HashMap<TypeHolder, (TC, TC, TC, TC) -> TC>()

    operator fun get(type: TypeHolder): TC? {
        when(type.arguments.size) {
            0 -> return default[type]
            1 -> {
                val argGen = get(type.arguments.first())
                argGen ?: return null
                return generic1[type.copy(arguments = emptyList())]?.invoke(argGen)
            }
            2 -> {
                val argGen0 = get(type.arguments[0])
                argGen0 ?: return null
                val argGen1 = get(type.arguments[1])
                argGen1 ?: return null
                return generic2[type.copy(arguments = emptyList())]?.invoke(argGen0, argGen1)
            }
            3 -> {
                val argGen0 = get(type.arguments[0])
                argGen0 ?: return null
                val argGen1 = get(type.arguments[1])
                argGen1 ?: return null
                val argGen2 = get(type.arguments[2])
                argGen2 ?: return null
                return generic3[type.copy(arguments = emptyList())]?.invoke(argGen0, argGen1, argGen2)
            }
            4 -> {
                val argGen0 = get(type.arguments[0])
                argGen0 ?: return null
                val argGen1 = get(type.arguments[1])
                argGen1 ?: return null
                val argGen2 = get(type.arguments[2])
                argGen2 ?: return null
                val argGen3 = get(type.arguments[3])
                argGen3 ?: return null
                return generic4[type.copy(arguments = emptyList())]?.invoke(argGen0, argGen1, argGen2, argGen3)
            }
            else -> throw Error("TypeClassContext for generic classes with more than 4 parameters not implemented yet")
        }
    }

    operator fun set(th: TypeHolder, tc: TC) {
        default[th] = tc
        if(!th.isNullable) {
            default[th.copy(isNullable = true)] = handleNullable(tc)
        }
        val arrayTC = handleArray(th.clazz, tc)
        generic1[TypeHolder(Array<Any?>::class.java, listOf(th), false, Mutability.NONE)] = { arrayTC }
        generic1[TypeHolder(Array<Any?>::class.java, listOf(th), true, Mutability.NONE)] = { handleNullable(arrayTC) }
    }

    operator fun set(th: TypeHolder, f: (TC) -> TC) {
        val type = th.copy(arguments = emptyList())
        generic1[type] = f
        if(!type.isNullable) {
            generic1[type.copy(isNullable = true)] = with(FancyFunctions){ f.mapResult{ handleNullable(it) } }
        }
    }

    operator fun set(th: TypeHolder, f: (TC, TC) -> TC) {
        val type = th.copy(arguments = emptyList())
        generic2[type] = f
        if(!type.isNullable) {
            generic2[type.copy(isNullable = true)] = with(FancyFunctions){ f.mapResult{ handleNullable(it) } }
        }
    }

    operator fun set(th: TypeHolder, f: (TC, TC, TC) -> TC) {
        val type = th.copy(arguments = emptyList())
        generic3[type] = f
        if(!type.isNullable) {
            generic3[type.copy(isNullable = true)] = with(FancyFunctions){ f.mapResult{ handleNullable(it) } }
        }
    }

    operator fun set(th: TypeHolder, f: (TC, TC, TC, TC) -> TC) {
        val type = th.copy(arguments = emptyList())
        generic4[type] = f
        if(!type.isNullable) {
            generic4[type.copy(isNullable = true)] = with(FancyFunctions){ f.mapResult{ handleNullable(it) } }
        }
    }

}