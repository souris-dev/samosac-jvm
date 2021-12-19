package com.sachett.slang.slangc.symbol

interface ISymbol {
    val name: String
    val symbolType: SymbolType
    val firstAppearedLine: Int

    fun isSymbolType(symbolType: SymbolType): Boolean
}
