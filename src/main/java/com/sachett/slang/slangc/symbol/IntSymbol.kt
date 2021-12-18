package com.sachett.slang.slangc.symbol

class IntSymbol(
    override val name: String,
    override val firstAppearedLine: Int,
    var value: Int = SymbolType.INT.defaultValue as Int
) : ISymbol {
    override val symbolType: SymbolType = SymbolType.INT

    override fun isSymbolType(symbolType: SymbolType): Boolean {
        return symbolType == SymbolType.INT
    }
}