package com.sachett.slang.slangc.staticchecker.analysers

import com.sachett.slang.slangc.symbol.FunctionSymbol

interface IFunctionInnerBlock {
    val doesReturnProperly: Boolean
    val parentFnSymbol: FunctionSymbol
    val children: ArrayList<IFunctionInnerBlock>
}