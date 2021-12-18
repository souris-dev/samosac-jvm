package com.sachett.slang.slangc.symbol

class StringSymbol(
    override val name: String,
    override val firstAppearedLine: Int,
    var value: String = SymbolType.STRING.defaultValue as String
) : ISymbol {
    override val symbolType: SymbolType = SymbolType.STRING

    override fun isSymbolType(symbolType: SymbolType): Boolean = symbolType == SymbolType.STRING
}