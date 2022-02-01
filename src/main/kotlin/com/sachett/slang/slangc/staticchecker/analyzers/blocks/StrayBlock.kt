package com.sachett.slang.slangc.staticchecker.analyzers.blocks

import com.sachett.slang.slangc.symbol.FunctionSymbol

/**
 * Represents a series of statements (without curly braces around them).
 * This is the leaf node type in the function path tree and does not contain any children.
 */
data class StrayBlock(
    override val parentFnSymbol: FunctionSymbol,
    override var doesReturnProperly: Boolean,
    override val children: ArrayList<IFunctionInnerBlock> = arrayListOf(),
    override val parent: IFunctionInnerBlock?
) : IFunctionInnerBlock