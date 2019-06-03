package ru.spbstu.kotlin.generate.combinators

import ru.spbstu.kotlin.generate.util.subst
import ru.spbstu.kotlin.typeclass.TypeClasses
import ru.spbstu.ktuples.Tuple
import ru.spbstu.ktuples.Tuple2
import ru.spbstu.ktuples.Tuple3
import ru.spbstu.ktuples.Tuple4
import ru.spbstu.wheels.tryEx
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.reflect

inline fun <reified T, reified R: Any> TypeClasses.deriveArbitrary(noinline body: (T) -> R) {
    val rbody = body.reflect()!!
    val tType = rbody.parameters.first().type
    val arbitrary = Arbitrary::class

    rawInstance(arbitrary, R::class) { etypes, _ ->
        val tGen = (arbitrary of tType) as Arbitrary<T>
        tGen.map(body)
    }
}

inline fun <reified A, reified B, reified R: Any> TypeClasses.deriveArbitrary(noinline body: (A, B) -> R) {
    val rbody = body.reflect()!!
    val tTypes = rbody.parameters.map { it.type }
    val arbitrary = Arbitrary::class

    rawInstance(arbitrary, R::class) { _, _ ->
        val tGen = Tuple.ofList(tTypes.map { arbitrary of it }) as Tuple2<Arbitrary<A>, Arbitrary<B>>
        arbitrary { body(tGen.v0.next(), tGen.v1.next()) }
    }
}

inline fun <reified A, reified B, reified C, reified R: Any> TypeClasses.deriveArbitrary(noinline body: (A, B, C) -> R) {
    val rbody = body.reflect()!!
    val tTypes = rbody.parameters.map { it.type }
    val arbitrary = Arbitrary::class

    rawInstance(arbitrary, R::class) { _, _ ->
        val tGen = Tuple.ofList(tTypes.map { arbitrary of it }) as Tuple3<Arbitrary<A>, Arbitrary<B>, Arbitrary<C>>
        arbitrary { body(tGen.v0.next(), tGen.v1.next(), tGen.v2.next()) }
    }
}

inline fun <reified A, reified B, reified C, reified D,
        reified R: Any> TypeClasses.deriveArbitrary(noinline body: (A, B, C, D) -> R) {
    val rbody = body.reflect()!!
    val tTypes = rbody.parameters.map { it.type }
    val arbitrary = Arbitrary::class

    rawInstance(arbitrary, R::class) { _, _ ->
        val tGen = Tuple.ofList(tTypes.map { arbitrary of it }) as Tuple4<Arbitrary<A>, Arbitrary<B>, Arbitrary<C>, Arbitrary<D>>
        arbitrary { body(tGen.v0.next(), tGen.v1.next(), tGen.v2.next(), tGen.v3.next()) }
    }
}

inline fun <reified T, reified R: Any> TypeClasses.deriveShrinker(noinline build: (T) -> R, noinline unbuild: (R) -> T) {
    val rbody = build.reflect()!!
    val tType = rbody.parameters.first().type
    val shrinker = Shrinker::class

    rawInstance(shrinker, R::class) { etypes, _ ->
        val tGen = (shrinker of tType) as Shrinker<T>
        tGen.bimap(build, unbuild)
    }
}

fun <T: Any> TypeClasses.deriveArbitraryForDataClass(klass: KClass<T>) {
    val arbitrary = Arbitrary::class
    require(klass.isData)
    val generics = klass.typeParameters.map { it.starProjectedType }

    val constructor = klass.primaryConstructor!!
    rawInstance(arbitrary, klass) { eTypes, _ ->
        val mapping = (generics zip eTypes).toMap()
        val arbitraries = constructor.parameters.map { arbitrary of it.type.subst(mapping) }
        arbitrary {
            constructor.call(*arbitraries.map { it.next() }.toTypedArray())
        }
    }
}

fun <T: Any> TypeClasses.deriveShrinkerForDataClass(klass: KClass<T>) {
    val shrinker = Shrinker::class
    require(klass.isData)
    val generics = klass.typeParameters.map { it.starProjectedType }

    val constructor = klass.primaryConstructor!!
    rawInstance(shrinker, klass) { eTypes, _ ->
        val mapping = (generics zip eTypes).toMap()
        val shrinkers = constructor.parameters.map { shrinker of it.type.subst(mapping) } as List<Shrinker<Any?>>
        val props = constructor.parameters.map { p -> klass.declaredMemberProperties.find { it.name == p.name }!! }
        shrinker { value ->
            val deconstruct = props.map { it.get(value) }
            shrinkMany(deconstruct, shrinkers).map {
                constructor.call(*it.toTypedArray())
            }
        }
    }
}

