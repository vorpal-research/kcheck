@file:Suppress("UNCHECKED_CAST")

package ru.spbstu.kotlin.generate.util

import java.lang.reflect.Array

data class ReflectedArray<T>(val element: Class<T>, val array: kotlin.Array<T>) {
    constructor(element: Class<T>, size: Int): this(element, Array.newInstance(element, size) as kotlin.Array<T>)
    constructor(element: Class<T>, size: Int, init: (Int) -> Any?):
            this(element, Array.newInstance(element, size) as kotlin.Array<T>) {
        (0..size).forEach { i ->
            Array.set(array, i, init(i))
        }
    }

    operator fun get(index: Int): T? = Array.get(array, index) as? T?
    operator fun set(index: Int, value: T?) = Array.set(array, index, value)
}

