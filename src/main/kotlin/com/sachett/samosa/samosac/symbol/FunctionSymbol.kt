package com.sachett.samosa.samosac.symbol

class FunctionSymbol(
    override val name: String,
    override val firstAppearedLine: Int,
    val paramList: ArrayList<ISymbol>,
    val returnType: SymbolType = SymbolType.VOID,
    override val isInferredType: Boolean = false,
    override var isInitialValueCalculated: Boolean = true,
    override var initializeExpressionPresent: Boolean = true,
    override var symbolCoordinates: Pair<Int, Int>? = null
) : ISymbol {
    override val symbolType: SymbolType = SymbolType.FUNCTION

    companion object {
        val allowedReturnTypes =
            listOf(SymbolType.INT, SymbolType.BOOL, SymbolType.STRING, SymbolType.VOID)
    }

    override fun isSymbolType(symbolType: SymbolType): Boolean = symbolType == SymbolType.FUNCTION
}