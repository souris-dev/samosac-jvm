package com.sachett.samosa.samosac.staticchecker.evaluators

interface IStaticExprEvaluator<T> {
    fun checkStaticEvaluable(): T
    fun evaluate(): T
}