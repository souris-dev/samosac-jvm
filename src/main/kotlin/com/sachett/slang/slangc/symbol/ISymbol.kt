package com.sachett.slang.slangc.symbol

interface ISymbol {
    val name: String
    val symbolType: SymbolType
    val firstAppearedLine: Int
    val isInferredType: Boolean // was type inferred for this symbol?
    var initializeExpressionPresent: Boolean // was the value
    var isInitialValueCalculated: Boolean
    var symbolCoordinates: Pair<Int, Int>? // coordinates of the symbol in the symbol table
    fun isSymbolType(symbolType: SymbolType): Boolean

    /**
     * Returns the symbol name augmented with its symbol table coordinates.
     */
    fun getAugmentedName(): String {
        return name + "__" + symbolCoordinates?.first + "_" + symbolCoordinates?.second
    }
}
