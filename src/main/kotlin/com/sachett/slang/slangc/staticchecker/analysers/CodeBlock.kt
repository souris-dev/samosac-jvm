package com.sachett.slang.slangc.staticchecker.analysers

import com.sachett.slang.parser.SlangBaseListener
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.staticchecker.ExpressionTypeDetector
import com.sachett.slang.slangc.symbol.FunctionSymbol
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable
import org.antlr.v4.runtime.tree.ParseTreeWalker

/**
 * Represents a code block (a series of statements WITH curly braces around them).
 */
class CodeBlock(
    override val parentFnSymbol: FunctionSymbol,
    override val parent: IFunctionInnerBlock,
    private val symbolTable: SymbolTable,
    private val blockCtx: SlangParser.BlockContext
) :
    IFunctionInnerBlock, SlangBaseListener() {
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

    override fun enterReturnStmtNoExpr(ctx: SlangParser.ReturnStmtNoExprContext?) {
        doesReturnProperly = parentFnSymbol.returnType == SymbolType.VOID
    }

    override fun enterReturnStmtWithExpr(ctx: SlangParser.ReturnStmtWithExprContext?) {
        val expressionTypeDetector = ExpressionTypeDetector(symbolTable)
        val (homoTypes, expType) = expressionTypeDetector.getType(ctx!!.expr())

        doesReturnProperly = homoTypes && expType == parentFnSymbol.returnType
    }
}