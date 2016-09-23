package ru.spbstu.kotlin.generate.assume

class AssumptionFailedException(): Exception("Assumption failed") {
    @Override
    fun fillInStackTrace(): Throwable = this
}

fun assume(condition: Boolean){ if(!condition) throw AssumptionFailedException() }
fun retry() = assume(false)
