package com.sachett.slang.slangc.symbol.symboltable

import com.sachett.slang.slangc.symbol.ISymbol

class SymbolTable {

    /* Stores the top-level table */
    private val symbolScope: ArrayList<ArrayList<SymbolTableRecordEntry>> = arrayListOf()

    /* Stores which scope of the top-level symbol table we are currently in */
    private var currentScopeIndex: Int = 0

    /* Stores (object reference to) current symbol table record entry */
    private var currentSymbolTableRecord: SymbolTableRecordEntry

    /* On next scope increment, should a new scope be created? */
    private var createNewScopeEntryOnIncrement: Boolean = true

    init {
        val globalEntry = SymbolTableRecordEntry(null)
        symbolScope.add(arrayListOf(globalEntry))
        currentSymbolTableRecord = globalEntry
    }

    /* Insert a symbol into the current scope */
    fun insert(name: String, symbol: ISymbol): Boolean {
        if (currentSymbolTableRecord.table.containsKey(name)) return false

        currentSymbolTableRecord.table[name] = symbol
        return true
    }

    /* Go one scope level up */
    fun incrementScope() {
        val newSymbolTableRecordEntry = SymbolTableRecordEntry(currentSymbolTableRecord)
        if (currentScopeIndex == (symbolScope.size - 1)) {
            symbolScope.add(arrayListOf(newSymbolTableRecordEntry))
            currentSymbolTableRecord = newSymbolTableRecordEntry
        }
        else {
            // implies that scope level was decreased previously

            if (createNewScopeEntryOnIncrement) {
                // create a new scope entry when increasing the scope
                symbolScope[currentScopeIndex + 1].add(newSymbolTableRecordEntry)
                currentSymbolTableRecord = newSymbolTableRecordEntry
            }
            else {
                // get the last scope entry in the next scope
                currentSymbolTableRecord = symbolScope[currentScopeIndex + 1].last()
                createNewScopeEntryOnIncrement = true // reset this flag
            }
        }
    }

    /* Go one scope level down */
    fun decrementScope(createNewScopeEntryOnNextIncrement: Boolean = true) {
        if (currentScopeIndex != 0 && currentSymbolTableRecord.prevScopeTable != null) {
            currentScopeIndex--
            currentSymbolTableRecord = currentSymbolTableRecord.prevScopeTable!!
        }

        createNewScopeEntryOnIncrement = createNewScopeEntryOnNextIncrement
    }

    fun lookup(name: String): ISymbol? {
        var tempScope: SymbolTableRecordEntry? = currentSymbolTableRecord

        while (tempScope != null && !tempScope.table.containsKey(name)) {
            tempScope = tempScope.prevScopeTable
        }

        return tempScope?.table?.get(name)
    }
}