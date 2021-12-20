package com.sachett.slang.slangc.symbol

interface ISymbol {
    val name: String
    val symbolType: SymbolType
    val firstAppearedLine: Int
    val isInferredType: Boolean // was type inferred for this symbol?

    fun isSymbolType(symbolType: SymbolType): Boolean
}
