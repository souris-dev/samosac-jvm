package com.sachett.slang.slangc.symbol

class FunctionSymbol(override val name: String, override val firstAppearedLine: Int) : ISymbol {
    override val symbolType: SymbolType = SymbolType.FUNCTION
    val returnType: SymbolType = SymbolType.VOID
    val paramList: ArrayList<ISymbol> = ArrayList()

    constructor(
        name: String,
        firstAppearedLine: Int,
        paramList: ArrayList<ISymbol>,
        returnType: SymbolType
    ) : this(name, firstAppearedLine)

    constructor(
        name: String,
        firstAppearedLine: Int,
        paramList: ArrayList<ISymbol>
    ) : this(name, firstAppearedLine)

    override fun isSymbolType(symbolType: SymbolType): Boolean = symbolType == SymbolType.FUNCTION
}