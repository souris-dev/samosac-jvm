package com.sachett.slang.slangc.symbol

class BoolSymbol(
    override val name: String,
    override val firstAppearedLine: Int,
    override val isInferredType: Boolean = false,
    var value: Boolean = SymbolType.BOOL.defaultValue as Boolean,
    override var isInitialValueCalculated: Boolean,
    override var initializeExpressionPresent: Boolean
) : ISymbol {
    override val symbolType: SymbolType = SymbolType.BOOL

    override fun isSymbolType(symbolType: SymbolType): Boolean = symbolType == SymbolType.BOOL
}