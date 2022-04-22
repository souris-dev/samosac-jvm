package com.sachett.slang.slangc.staticchecker.evaluators

interface IStaticExprEvaluator<T> {
    fun checkStaticEvaluable(): T
    fun evaluate(): T
}