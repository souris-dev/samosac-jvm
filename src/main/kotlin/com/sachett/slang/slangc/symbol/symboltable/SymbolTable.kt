package com.sachett.slang.slangc.symbol.symboltable

import com.sachett.slang.slangc.symbol.ISymbol

class SymbolTable {

    /* Stores the top-level table */
    private val symbolScope: ArrayList<ArrayList<SymbolTableRecordEntry>> = arrayListOf()

    /**
     * Stores a map of blocks with their scope coordinates in the table for quick access.
     * The whole block context is not stored; rather, only the start of the block's position is stored.
     * The start of the block's position is indicated by the '{' character's position
     * as a pair: (line number, character number).
     */
    private val blockScopes: MutableMap<Pair<Int, Int>, Pair<Int, Int>?> = mutableMapOf()

    /* Stores which scope of the top-level symbol table we are currently in */
    private var currentScopeCoordinates: Pair<Int, Int> = Pair(0, 0)

    /* Stores (object reference to) current symbol table record entry */
    private var currentSymbolTableRecord: SymbolTableRecordEntry

    /* On next scope increment, should a new scope be created? */
    private var createNewScopeEntryOnIncrement: Boolean = true

    /* When coordinates are changed manually, this stack keeps track of the coordinates */
    private var lastCoordinates: java.util.ArrayDeque<Pair<Int, Int>> = java.util.ArrayDeque()

    init {
        val globalEntry = SymbolTableRecordEntry(null, prevScopeIndex = -1, scopeIndex = 0)
        globalEntry.recordEntryCoordinates = Pair(0, 0)
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
            currentSymbolTableRecord, prevScopeIndex = currentScopeCoordinates.first,
            scopeIndex = currentScopeCoordinates.first + 1
        )
        if (currentScopeCoordinates.first == (symbolScope.size - 1)) {
            currentScopeCoordinates = Pair(currentScopeCoordinates.first + 1, currentScopeCoordinates.second)
            newSymbolTableRecordEntry.recordEntryCoordinates = Pair(currentScopeCoordinates.first, 0)
            symbolScope.add(arrayListOf(newSymbolTableRecordEntry))
            currentSymbolTableRecord = newSymbolTableRecordEntry
        } else {
            // implies that scope level was decreased previously

            if (createNewScopeEntryOnIncrement) {
                // create a new scope entry when increasing the scope
                currentScopeCoordinates = Pair(currentScopeCoordinates.first + 1, currentScopeCoordinates.second)
                // since this is being appended, coordinates
                // = (currentScopeIndex, <size of this symbolScope - 1 (for 0-based indexing)>)
                newSymbolTableRecordEntry.recordEntryCoordinates = Pair(
                    currentScopeCoordinates.first, symbolScope[currentScopeCoordinates.first].size
                )
                symbolScope[currentScopeCoordinates.first].add(newSymbolTableRecordEntry)
                currentSymbolTableRecord = newSymbolTableRecordEntry
            } else {
                // get the last scope entry in the next scope
                currentScopeCoordinates = Pair(currentScopeCoordinates.first + 1, currentScopeCoordinates.second)
                currentSymbolTableRecord = symbolScope[currentScopeCoordinates.first].last()
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
            currentSymbolTableRecord, prevScopeIndex = currentScopeCoordinates.first,
            scopeIndex = currentScopeCoordinates.first + 1
        )
        if (currentScopeCoordinates.first == (symbolScope.size - 1)) {
            currentScopeCoordinates = Pair(currentScopeCoordinates.first + 1, currentScopeCoordinates.second)
            newSymbolTableRecordEntry.recordEntryCoordinates = Pair(currentScopeCoordinates.first, 0)
            symbolScope.add(arrayListOf(newSymbolTableRecordEntry))
            currentSymbolTableRecord = newSymbolTableRecordEntry
        } else {
            // implies that scope level was decreased previously
            if (createNewScopeEntry) {
                // create a new scope entry when increasing the scope
                currentScopeCoordinates = Pair(currentScopeCoordinates.first + 1, currentScopeCoordinates.second)
                // since this is being appended, coordinates
                // = (currentScopeIndex, <size of this symbolScope - 1 (for 0-based indexing)>)
                newSymbolTableRecordEntry.recordEntryCoordinates = Pair(
                    currentScopeCoordinates.first, symbolScope[currentScopeCoordinates.first].size
                )
                symbolScope[currentScopeCoordinates.first].add(newSymbolTableRecordEntry)
                currentSymbolTableRecord = newSymbolTableRecordEntry
            } else {
                // get the last scope entry in the next scope
                currentScopeCoordinates = Pair(currentScopeCoordinates.first + 1, currentScopeCoordinates.second)
                currentSymbolTableRecord = symbolScope[currentScopeCoordinates.first].last()
                createNewScopeEntryOnIncrement = true // reset this flag
            }
        }
    }

    /**
     * Sets current scope index to 0 and currently pointed scope record to first record.
     */
    fun resetScopeIndex() {
        currentScopeCoordinates = Pair(0, 0)
        currentSymbolTableRecord = symbolScope[0][0]
    }

    /* Go one scope level back */
    @JvmOverloads
    fun decrementScope(createNewScopeEntryOnNextIncrement: Boolean = true) {
        if (currentScopeCoordinates.first != 0 && currentSymbolTableRecord.prevScopeTable != null) {
            currentScopeCoordinates = Pair(currentScopeCoordinates.first - 1, currentScopeCoordinates.second)
            currentSymbolTableRecord = currentSymbolTableRecord.prevScopeTable!!
        }

        this.createNewScopeEntryOnIncrement = createNewScopeEntryOnNextIncrement
    }

    /**
     * Recursively looks up a symbol.
     * @param   name    The symbol name to look up.
     */
    fun lookup(name: String): ISymbol? {
        var tempScope: SymbolTableRecordEntry? = currentSymbolTableRecord

        while (tempScope != null && !tempScope.table.containsKey(name)) {
            tempScope = tempScope.prevScopeTable
        }
        return tempScope?.table?.get(name)
    }

    /**
     * Register a block with its symbol table coordinates as the current coordinates.
     * @param   blockStart  A pair indicating the starting position of the block (the '{' character's position).
     */
    fun registerBlockInCurrentCoordinates(blockStart: Pair<Int, Int>) {
        if (blockScopes.containsKey(blockStart)) return
        blockScopes[blockStart] = currentSymbolTableRecord.recordEntryCoordinates
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
     * @param   name    The name of the symbol to look up.
     */
    fun lookupInCurrentScopeOnly(name: String): ISymbol? {
        return if (currentSymbolTableRecord.table.containsKey(name)) {
            currentSymbolTableRecord.table[name]
        } else {
            null
        }
    }

    /**
     * Goes to mentioned scope coordinates in the symbol table. Note: Prone to exception if wrong coordinates passed.
     * @param   coordinates The coordinates to go to.
     */
    fun goToCoordinates(coordinates: Pair<Int, Int>) {
        lastCoordinates.push(currentScopeCoordinates)
        currentScopeCoordinates = coordinates
        currentSymbolTableRecord = symbolScope[coordinates.first][coordinates.second]
    }

    /**
     * Goes to mentioned block's scope coordinates in the symbol table. Note: Prone to exception if bad block passed.
     * @param   blockStart The starting position of the block (i.e., position of the '}' character).
     */
    fun goToBlock(blockStart: Pair<Int, Int>) {
        val blockCoordinates = blockScopes[blockStart]!!
        goToCoordinates(blockCoordinates)
    }

    /**
     * If previously goToCoordinates or goToBlock was used to navigate to some other coordinates,
     * this function can be called to restore the last coordinates
     * (the coordinates before goToCoordinates was called).
     */
    fun restoreLastCoordinates() {
        currentScopeCoordinates = lastCoordinates.pop()
        currentSymbolTableRecord = symbolScope[currentScopeCoordinates.first][currentScopeCoordinates.second]
    }
}