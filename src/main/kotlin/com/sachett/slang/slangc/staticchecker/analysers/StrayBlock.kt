package com.sachett.slang.slangc.staticchecker.analysers

import com.sachett.slang.slangc.symbol.FunctionSymbol

/**
 * Represents a series of statements (without curly braces around them).
 */
data class StrayBlock(
    override val parentFnSymbol: FunctionSymbol,
    override val doesReturnProperly: Boolean,
    override val children: ArrayList<IFunctionInnerBlock> = arrayListOf()
) : IFunctionInnerBlock