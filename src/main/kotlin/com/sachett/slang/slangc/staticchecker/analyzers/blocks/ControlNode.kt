package com.sachett.slang.slangc.staticchecker.analyzers.blocks

import com.sachett.slang.parser.SlangBaseListener
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.symbol.FunctionSymbol

abstract class ControlNode(
    override val parentFnSymbol: FunctionSymbol,
    override val parent: IFunctionInnerBlock,
) :
    IFunctionInnerBlock, SlangBaseListener() {
    override val children: ArrayList<IFunctionInnerBlock> = arrayListOf()
}