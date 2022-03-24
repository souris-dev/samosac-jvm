package com.sachett.samosa.samosac.staticchecker.analyzers.blocks

import com.sachett.samosa.parser.SamosaBaseListener
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.symbol.FunctionSymbol

abstract class ControlNode(
    override val parentFnSymbol: FunctionSymbol,
    override val parent: IFunctionInnerBlock,
) :
    IFunctionInnerBlock, SamosaBaseListener() {
    override val children: ArrayList<IFunctionInnerBlock> = arrayListOf()
}