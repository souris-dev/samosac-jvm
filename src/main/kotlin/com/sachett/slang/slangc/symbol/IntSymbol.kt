package com.sachett.slang.slangc.symbol

class IntSymbol(
    override val name: String,
    override val firstAppearedLine: Int,
    override val isInferredType: Boolean = false,
    var value: Int = SymbolType.INT.defaultValue as Int,
    override var isInitialValueCalculated: Boolean,
    override var initializeExpressionPresent: Boolean,
    override var symbolCoordinates: Pair<Int, Int>? = null
) : ISymbol {
    override val symbolType: SymbolType = SymbolType.INT

    override fun isSymbolType(symbolType: SymbolType): Boolean {
        return symbolType == SymbolType.INT
    }
}