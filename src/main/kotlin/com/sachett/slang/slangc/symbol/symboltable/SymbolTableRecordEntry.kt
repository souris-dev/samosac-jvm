package com.sachett.slang.slangc.symbol.symboltable

import com.sachett.slang.slangc.symbol.ISymbol

data class SymbolTableRecordEntry(
    var prevScopeTable: SymbolTableRecordEntry?,
    val table: MutableMap<String, ISymbol> = mutableMapOf(),
    var prevScopeIndex: Int
)
