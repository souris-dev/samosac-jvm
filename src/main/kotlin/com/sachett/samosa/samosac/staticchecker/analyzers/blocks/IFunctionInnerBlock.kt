package com.sachett.samosa.samosac.staticchecker.analyzers.blocks

import com.sachett.samosa.samosac.symbol.FunctionSymbol

interface IFunctionInnerBlock {
    var doesReturnProperly: Boolean
    val parentFnSymbol: FunctionSymbol
    val children: ArrayList<IFunctionInnerBlock>
    val parent: IFunctionInnerBlock?
}