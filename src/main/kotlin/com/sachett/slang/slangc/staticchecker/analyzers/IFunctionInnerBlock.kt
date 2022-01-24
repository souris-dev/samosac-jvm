package com.sachett.slang.slangc.staticchecker.analyzers

import com.sachett.slang.slangc.symbol.FunctionSymbol

interface IFunctionInnerBlock {
    var doesReturnProperly: Boolean
    val parentFnSymbol: FunctionSymbol
    val children: ArrayList<IFunctionInnerBlock>
    val parent: IFunctionInnerBlock?
}