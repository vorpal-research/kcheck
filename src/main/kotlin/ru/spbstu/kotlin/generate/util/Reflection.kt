package ru.spbstu.kotlin.generate.util

import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.full.defaultType
import kotlin.reflect.jvm.reflect

fun KType.copy(classifier: KClassifier? = this.classifier,
               arguments: List<KTypeProjection> = this.arguments,
               nullable: Boolean = this.isMarkedNullable,
               annotations: List<Annotation> = this.annotations) =
        when (classifier) {
            null -> throw IllegalArgumentException("Unsupported classifier: null")
            else -> classifier.createType(arguments, nullable, annotations)
        }

inline fun KType.mapArguments(mapper: (KType) -> KType) = when {
    arguments.isEmpty() -> this
    else -> copy(arguments = arguments.map { it.copy(type = it.type?.let(mapper)) })
}

val KType.supertypes get() = when (classifier) {
    is KClass<*> -> {
        val klass = classifier as KClass<*>
        val raw = klass.defaultType // yes, we need this deprecated guy here
        val argMap = (raw.arguments zip this.arguments).map { (a, b) -> a.type to b.type }.toMap()
        klass.supertypes.map { it.mapArguments { argMap[it] ?: it } }
    }
    is KTypeParameter -> (classifier as KTypeParameter).upperBounds
    else -> throw IllegalArgumentException("Unsupported classifier: $classifier")
}

inline fun <reified T> typeOf(noinline body: () -> T) = body.reflect()!!.returnType

fun KType.subst(mapping: Map<KType, KType>): KType = when {
    this in mapping -> mapping.getValue(this)
    else -> mapArguments { it.subst(mapping) }
}
