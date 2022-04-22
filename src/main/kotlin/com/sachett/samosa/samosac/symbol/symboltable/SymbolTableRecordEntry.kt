package com.sachett.samosa.samosac.symbol.symboltable

import com.sachett.samosa.samosac.symbol.ISymbol

data class SymbolTableRecordEntry(
    var prevScopeTable: SymbolTableRecordEntry?,
    val table: MutableMap<String, ISymbol> = mutableMapOf(),
    var prevScopeIndex: Int,

    // TODO: remove scopeIndex because it is redundant
    var scopeIndex: Int,
    var recordEntryCoordinates: Pair<Int, Int>? = null
)
