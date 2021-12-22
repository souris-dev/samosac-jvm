package com.sachett.slang.slangc.staticchecker.blockutils

import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.symbol.SymbolType

/**
 * Represents a block inside a function body.
 */
data class FunctionInnerBlock(
    val blockCtx: SlangParser.BlockContext,
    var hasReturn: Boolean = false,
    var returnsType: SymbolType? = null,
    val childrenBlocks: ArrayList<FunctionInnerBlock?> = arrayListOf(),
    var parentBlock: FunctionInnerBlock? = null
)