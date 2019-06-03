package ru.spbstu.kotlin.generate

import kotlin.math.round

@Target(AnnotationTarget.TYPE)
annotation class InRange(val from: Long, val to: Long)
@Target(AnnotationTarget.TYPE)
annotation class InFloatRange(val from: Double, val to: Double)
@Target(AnnotationTarget.TYPE)
annotation class SizeInRange(val from: Int, val to: Int)
@Target(AnnotationTarget.TYPE)
annotation class Integral
@Target(AnnotationTarget.TYPE)
annotation class Finite
@Target(AnnotationTarget.TYPE)
annotation class FloatUnit(val value: Double)
@Target(AnnotationTarget.TYPE)
annotation class FromAlphabet(val alphabet: String)
@Target(AnnotationTarget.TYPE)
annotation class InCharRange(val from: Char, val to: Char)

operator fun InRange.contains(v: Int) = v in from..to
operator fun InRange.contains(v: Long) = v in from..to
operator fun InRange.contains(v: Short) = v in from..to
operator fun InRange.contains(v: Byte) = v in from..to
operator fun InRange.contains(v: Double) = from <= v && v <= to
operator fun InRange.contains(v: Float) = from <= v && v <= to

operator fun InFloatRange.contains(v: Double) = v in from..to
operator fun InFloatRange.contains(v: Float) = v in from..to

operator fun Integral.contains(v: Double) = v.isFinite() && round(v) == v
operator fun Integral.contains(v: Float) = v.isFinite() && round(v) == v

operator fun SizeInRange.contains(v: Int) = v in from..to

