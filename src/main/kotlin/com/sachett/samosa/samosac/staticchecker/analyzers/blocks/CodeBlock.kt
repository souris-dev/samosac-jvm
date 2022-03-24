package com.sachett.samosa.samosac.staticchecker.analyzers.blocks

import com.sachett.samosa.parser.SamosaBaseListener
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.staticchecker.ExpressionTypeDetector
import com.sachett.samosa.samosac.symbol.FunctionSymbol
import com.sachett.samosa.samosac.symbol.SymbolType
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable
import org.antlr.v4.runtime.tree.ParseTreeWalker

/**
 * Represents a code block (a series of statements WITH curly braces around them).
 */
class CodeBlock(
    override val parentFnSymbol: FunctionSymbol,
    override val parent: IFunctionInnerBlock,
    private val symbolTable: SymbolTable,
    private val blockCtx: SamosaParser.BlockContext
) :
    IFunctionInnerBlock, SamosaBaseListener() {
    private var doesReturnComputed = false

    override val children: ArrayList<IFunctionInnerBlock> = arrayListOf()

    override var doesReturnProperly: Boolean = false
        get() {
            if (doesReturnComputed) {
                return field
            }

            ParseTreeWalker.DEFAULT.walk(this, blockCtx)
            doesReturnComputed = true
            return field
        }
        set(value) {
            doesReturnComputed = true
            field = value
        }

    override fun enterReturnStmtNoExpr(ctx: SamosaParser.ReturnStmtNoExprContext?) {
        doesReturnProperly = parentFnSymbol.returnType == SymbolType.VOID
    }

    override fun enterReturnStmtWithExpr(ctx: SamosaParser.ReturnStmtWithExprContext?) {
        val expressionTypeDetector = ExpressionTypeDetector(symbolTable)
        val (homoTypes, expType) = expressionTypeDetector.getType(ctx!!.expr())

        doesReturnProperly = homoTypes && expType == parentFnSymbol.returnType
    }
}