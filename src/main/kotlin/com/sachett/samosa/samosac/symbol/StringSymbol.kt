package com.sachett.samosa.samosac.symbol

class StringSymbol(
    override val name: String,
    override val firstAppearedLine: Int,
    override val isInferredType: Boolean = false,
    var value: String = SymbolType.STRING.defaultValue as String,
    override var isInitialValueCalculated: Boolean,
    override var initializeExpressionPresent: Boolean,
    override var symbolCoordinates: Pair<Int, Int>? = null
) : ISymbol {
    override val symbolType: SymbolType = SymbolType.STRING

    override fun isSymbolType(symbolType: SymbolType): Boolean = symbolType == SymbolType.STRING
}