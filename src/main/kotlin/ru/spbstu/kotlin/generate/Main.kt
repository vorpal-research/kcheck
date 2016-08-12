package ru.spbstu.kotlin.generate

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.defaultType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.reflect

//object Gens2{
//    val randomGen = Random()


//
//    init {
//        setDefault(Int::class, defaultForInt())
//        setDefault(Short::class, defaultForShort())
//        setDefault(Long::class, defaultForLong())
//        setDefault(Boolean::class, defaultForBoolean())
//        setDefault(Float::class, defaultForFloat())
//        setDefault(Double::class, defaultForDouble())
//        setDefault(Number::class, defaultForNumber())
//        setDefault(Any::class, ofFun { Any() })
//
//        setDefault(String::class, defaultForString());
//
//        genericGens1[List::class.java] = {
//            param -> priorities(
//                1 to ofFun { listOf<Any?>() },
//                1 to ofFun { listOf(param.next()) },
//                3 to ofFun {
//                    val len = randomGen.nextInt(20)
//                    IntRange(0, len).map { param.next() }.toList()
//                },
//                3 to ofFun {
//                    val len = randomGen.nextInt(255)
//                    IntRange(0, len).map { param.next() }.toList()
//                }
//        )
//        }
//
//        genericGens1[Set::class.java] = {
//            param -> priorities(
//                1 to ofFun { setOf<Any?>() },
//                1 to ofFun { setOf(param.next()) },
//                3 to ofFun {
//                    val len = randomGen.nextInt(20)
//                    IntRange(0, len).map { param.next() }.toSet()
//                },
//                3 to ofFun {
//                    val len = randomGen.nextInt(255)
//                    IntRange(0, len).map { param.next() }.toSet()
//                }
//        )
//        }
//
//        genericGens1[Collection::class.java] = genericGens1[List::class.java]!!
//        genericGens1[Iterable::class.java] = genericGens1[List::class.java]!!
//
//        genericGens2[Pair::class.java] = {
//            param0, param1 -> ofFun { Pair(param0.next(), param1.next()) }
//        }
//
//        genericGens2[Map::class.java] = {
//            kgen, vgen -> priorities(
//                1 to ofFun { mapOf<Any?, Any?>() },
//                1 to ofFun { mapOf(kgen.next() to vgen.next()) },
//                3 to ofFun {
//                    val len = randomGen.nextInt(20)
//                    IntRange(0, len).map { kgen.next() to vgen.next() }.toMap()
//                },
//                3 to ofFun {
//                    val len = randomGen.nextInt(255)
//                    IntRange(0, len).map { kgen.next() to vgen.next() }.toMap()
//                }
//        )
//        }
//
//        genericGens2[MutableMap::class.java] = {
//            kgen, vgen -> priorities(
//                1 to ofFun { mutableMapOf<Any?, Any?>() } as Gen<Nothing?>,
//                1 to ofFun { mutableMapOf(kgen.next() to vgen.next()) } as Gen<Nothing?>,
//                3 to ofFun {
//                    val len = randomGen.nextInt(20)
//                    IntRange(0, len).map { kgen.next() to vgen.next() }.toMap(java.util.LinkedHashMap<Any?, Any?>())
//                } as Gen<Nothing?>,
//                3 to ofFun {
//                    val len = randomGen.nextInt(255)
//                    IntRange(0, len).map { kgen.next() to vgen.next() }.toMap(java.util.LinkedHashMap<Any?, Any?>())
//                } as Gen<Nothing?>
//        )
//        }
//    }
//
//    val Type.erased: Class<*>
//        get() = when(this) {
//            is Class<*> -> this
//            is WildcardType -> upperBounds.first().erased
//            is GenericArrayType -> java.lang.reflect.Array.newInstance(genericComponentType.erased, 0).javaClass // this is bullshit
//            is ParameterizedType -> rawType.erased
//            else -> TODO("Unsupported type: $this")
//        }
//
//    fun getDefault(t: Type, isNullable: Boolean = false): Gen<*>? = {
//        when(t){
//            is Class<*> ->
//                if(t.isArray) {
//                    val elem = getDefault(t.componentType)
//                    if(elem == null) null
//                    else arrayGen(t.componentType.erased, elem)
//                } else if(isNullable) {
//                    defaultGens[t]?.run {
//                        priorities(5 to (this as Gen<Nothing?>), 1 to constant(null))
//                    }
//                } else defaultGens[t]
//            is WildcardType -> getDefault(t.upperBounds.first(), isNullable)
//            is GenericArrayType -> {
//                val elem = getDefault(t.genericComponentType)
//                if(elem == null) null
//                else arrayGen(t.genericComponentType.erased, elem)
//            }
//            is ParameterizedType -> {
//                val size = t.actualTypeArguments.size
//                val args = t.actualTypeArguments.map{t: Type -> getDefault(t)!!} ?: ArrayList<Gen<*>>()
//                when(size) {
//                    1 -> genericGens1[t.rawType]?.invoke(args[0])
//                    2 -> genericGens2[t.rawType]?.invoke(args[0], args[1])
//                    3 -> genericGens3[t.rawType]?.invoke(args[0], args[1], args[2])
//                    4 -> genericGens4[t.rawType]?.invoke(args[0], args[1], args[2], args[3])
//                    else -> TODO("type $t not currently supported, sorry")
//                }
//            }
//            else -> TODO("type $t not currently supported, sorry")
//        }
//    }()
//
//    inline fun<reified T: Any> forAll(noinline f: (T) -> Boolean) : Boolean {
//        val gens = getDefault(typeFor<T>(), f.reflect()?.parameters?.first()?.type?.isMarkedNullable ?: false) as Gen<T>
//
//        return IntRange(0, 1000).all { iteration ->
//            val param = gens.next()
//            try {
//                if(!f.invoke(param)) {
//                    println("Stopped on $param after ${iteration} iterations")
//                    false
//                } else true
//            } catch(ex: Exception) {
//                println("Stopped on exception after ${iteration} iterations")
//                ex.printStackTrace(System.out)
//                false
//            }
//        }
//    }
//
//    inline fun<reified T: Any, reified U: Any> forAll(noinline f: (T, U) -> Boolean) : Boolean {
//        val gen0 = getDefault(typeFor<T>(), f.reflect()?.parameters?.get(0)?.type?.isMarkedNullable ?: false) as Gen<T>
//        val gen1 = getDefault(typeFor<U>(), f.reflect()?.parameters?.get(1)?.type?.isMarkedNullable ?: false) as Gen<U>
//
//        return IntRange(0, 100000).all { iteration ->
//            val param0 = gen0.next()
//            val param1 = gen1.next()
//            try {
//                if (!f.invoke(param0, param1)) {
//                    println("Stopped on ($param0, $param1) after ${iteration} iterations")
//                    false
//                } else true
//            } catch(ex: Exception) {
//                println("Stopped on exception after ${iteration} iterations")
//                ex.printStackTrace(System.out)
//                false
//            }
//        }
//    }
//
//}
//
//inline fun<reified T> classFor(v: T) = T::class
//
//fun main(args: Array<String>) {
//    println(Gens.forAll { i: Int -> i < i + 1 })
//    println(Gens.forAll { d: Double -> d <= 0 || d > 0 })
//
//    println(Gens.forAll { i: Int, j: Int -> i > j || i < j })
//    println(Gens.forAll { t: Double -> t == t  })
//    println(Gens.forAll { t: Double -> t.equals(t)  })
//
//    println(Gens.forAll { t: Double -> t == (t + 1) - 1  })
//    println(Gens.forAll { t: Double, u: Double -> t / u * u == t })
//    println(Gens.forAll { t: Int, u: Int -> t / u * u == t })
//    println(Gens.forAll { t: Int, u: Int -> t / u == t / u })
//    println(Gens.forAll { t: Int? -> t != null })
//
//    println(Gens.forAll { x: Any? -> x.toString().length > 0 })
//
//    println(Gens.forAll { x: Number -> x == x })
//    println(Gens.forAll { x: List<Int> -> x.size > 0 })
//    println(Gens.forAll { x: List<Int>? -> x?.size != 2 })
//
//    println(Gens.forAll { pr: Pair<Int, Double> -> pr.first.toDouble() != pr.second + 3 })
//    println(Gens.forAll { pr: Pair<Int, List<Int>> -> pr.second.first() != pr.first  })
//
//    println(Gens.forAll { i: Int -> i != 42  })
//
//    println(Gens.forAll { i: Int -> i != null })
//    println(Gens.forAll { i: Int? -> i != null })
//
//    println(Gens.forAll { lst: List<Int>? -> lst?.sorted() == lst?.sorted()?.sorted() })
//    println(Gens.forAll { lst: Collection<Int>? -> lst?.sorted() == lst?.sorted()?.sorted() })
//
//    println(Gens.forAll { s : String -> s.trim() == s.toString() })
//
//    println(Gens.forAll { m : Map<Double, String>, k : Double -> m.count { it.key == k } == 0 })
//
//    println(Gens.forAll { l: Long -> java.lang.Double.doubleToLongBits(java.lang.Double.longBitsToDouble(l)) == l })
//    println(Gens.forAll { d: Double -> java.lang.Double.longBitsToDouble(java.lang.Double.doubleToLongBits(d)) == d })
//
//    println(Gens.forAll { i: Int -> i.toDouble().toInt() == i })
//
//    println(Gens.forAll { a: Any? -> a.toString() == a?.toString() })
//
//    println(Gens.forAll { a: List<Any> -> a.filterNotNull().toList() == a })
//    println(Gens.forAll { a: List<Any?> -> a.filterNotNull().toList() == a })
//
//    println(Gens.forAll { a: Array<Array<String>> -> a.flatMap { it.asIterable() }.toSet().size > 3 })
//
//    println(Gens.forAll { a: Array<List<Int>> -> a.flatMap { it }.any { it < 56 || it > 100  } })
//
//    println(Gens.forAll { a: Array<String> -> a.toSet().size == a.size  })
//
//    println(Gens.forAll { a: List<String> -> a.toSet().size == a.size  })
//
//    println(Gens.forAll { a: String, b: String -> (a+b).length >= a.length  })
//
//    println(Gens.forAll { a: List<Int>, b: List<Int> -> (a.zip(b)).reversed() == a.reversed().zip(b.reversed())  })
//
//    println(Gens.forAll { d: Double -> d == d  })
//}