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
        val globalEntry = SymbolTableRecordEntry(null, prevScopeIndex = -1, scopeIndex = 0)
        symbolScope.add(arrayListOf(globalEntry))
        currentSymbolTableRecord = globalEntry
    }

    /* Insert a symbol into the current scope */
    fun insert(name: String, symbol: ISymbol): Boolean {
        if (currentSymbolTableRecord.table.containsKey(name)) return false
        symbol.symbolCoordinates = currentSymbolTableRecord.recordEntryCoordinates
        currentSymbolTableRecord.table[name] = symbol
        return true
    }

    /**
     * Go one scope level in.
     */
    fun incrementScope() {
        val newSymbolTableRecordEntry = SymbolTableRecordEntry(
            currentSymbolTableRecord, prevScopeIndex = currentScopeIndex,
            scopeIndex = currentScopeIndex + 1
        )
        if (currentScopeIndex == (symbolScope.size - 1)) {
            currentScopeIndex++
            newSymbolTableRecordEntry.recordEntryCoordinates = Pair(currentScopeIndex, 0)
            symbolScope.add(arrayListOf(newSymbolTableRecordEntry))
            currentSymbolTableRecord = newSymbolTableRecordEntry
        } else {
            // implies that scope level was decreased previously

            if (createNewScopeEntryOnIncrement) {
                // create a new scope entry when increasing the scope
                currentScopeIndex++
                // since this is being appended, coordinates
                // = (currentScopeIndex, <size of this symbolScope - 1 (for 0-based indexing)>)
                newSymbolTableRecordEntry.recordEntryCoordinates = Pair(currentScopeIndex, symbolScope.size - 1)
                symbolScope[currentScopeIndex].add(newSymbolTableRecordEntry)
                currentSymbolTableRecord = newSymbolTableRecordEntry
            } else {
                // get the last scope entry in the next scope
                currentScopeIndex++
                currentSymbolTableRecord = symbolScope[currentScopeIndex].last()
                createNewScopeEntryOnIncrement = true // reset this flag
            }
        }
    }

    /**
     * Go one scope level in.
     * @param   createNewScopeEntry control creation of new scope on increment
     *                              irrespective of createNewScopeEntryOnNextIncrement
     *                              in case currentScopeIndex != (symbolScope.size) - 1.
     */
    fun incrementScopeOverrideScopeCreation(createNewScopeEntry: Boolean = true) {
        val newSymbolTableRecordEntry = SymbolTableRecordEntry(
            currentSymbolTableRecord, prevScopeIndex = currentScopeIndex,
            scopeIndex = currentScopeIndex + 1
        )
        if (currentScopeIndex == (symbolScope.size - 1)) {
            currentScopeIndex++
            newSymbolTableRecordEntry.recordEntryCoordinates = Pair(currentScopeIndex, 0)
            symbolScope.add(arrayListOf(newSymbolTableRecordEntry))
            currentSymbolTableRecord = newSymbolTableRecordEntry
        } else {
            // implies that scope level was decreased previously
            if (createNewScopeEntry) {
                // create a new scope entry when increasing the scope
                currentScopeIndex++
                // since this is being appended, coordinates
                // = (currentScopeIndex, <size of this symbolScope - 1 (for 0-based indexing)>)
                newSymbolTableRecordEntry.recordEntryCoordinates = Pair(currentScopeIndex, symbolScope.size - 1)
                symbolScope[currentScopeIndex].add(newSymbolTableRecordEntry)
                currentSymbolTableRecord = newSymbolTableRecordEntry
            } else {
                // get the last scope entry in the next scope
                currentScopeIndex++
                currentSymbolTableRecord = symbolScope[currentScopeIndex].last()
                createNewScopeEntryOnIncrement = true // reset this flag
            }
        }
    }

    /**
     * Sets current scope index to 0 and currently pointed scope record to first record.
     */
    fun resetScopeIndex() {
        currentScopeIndex = 0
        currentSymbolTableRecord = symbolScope[0][0]
    }

    /* Go one scope level back */
    @JvmOverloads
    fun decrementScope(createNewScopeEntryOnNextIncrement: Boolean = true) {
        if (currentScopeIndex != 0 && currentSymbolTableRecord.prevScopeTable != null) {
            currentScopeIndex--
            currentSymbolTableRecord = currentSymbolTableRecord.prevScopeTable!!
        }

        this.createNewScopeEntryOnIncrement = createNewScopeEntryOnNextIncrement
    }

    fun lookup(name: String): ISymbol? {
        var tempScope: SymbolTableRecordEntry? = currentSymbolTableRecord

        while (tempScope != null && !tempScope.table.containsKey(name)) {
            tempScope = tempScope.prevScopeTable
        }
        return tempScope?.table?.get(name)
    }

    /**
     * Looks up and returns a pair the symbol with the scopeIndex of the
     * scope it is in, in order.
     * Returns a pair of nulls if the symbol is not found.
     */
    fun lookupWithNearestScopeValue(name: String): Pair<ISymbol?, Int?> {
        var tempScope: SymbolTableRecordEntry? = currentSymbolTableRecord

        while (tempScope != null && !tempScope.table.containsKey(name)) {
            tempScope = tempScope.prevScopeTable
        }

        return Pair(tempScope?.table?.get(name), tempScope?.scopeIndex)
    }

    /**
     * Looks up a symbol in the SymbolTableRecordEntry having the specified coordinates.
     * @param coordinates Coordinates of the SymbolTableRecordEntry.
     */
    fun lookupInCoordinates(name: String, coordinates: Pair<Int, Int>): ISymbol? {
        return try {
            val tempScope: SymbolTableRecordEntry = symbolScope[coordinates.first][coordinates.second]
            if (tempScope.table.containsKey(name)) {
                tempScope.table[name]
            } else {
                null
            }
        } catch (e: IndexOutOfBoundsException) {
            // TODO: throw?
            null
        }
    }

    /**
     * Looks up a symbol in the current SymbolTableRecordEntry (that is, in the current scope only).
     */
    fun lookupInCurrentScopeOnly(name: String): ISymbol? {
        return if (currentSymbolTableRecord.table.containsKey(name)) {
            currentSymbolTableRecord.table[name]
        } else {
            null
        }
    }
}