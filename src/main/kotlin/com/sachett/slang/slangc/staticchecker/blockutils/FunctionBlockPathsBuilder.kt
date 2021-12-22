package com.sachett.slang.slangc.staticchecker.blockutils

import com.sachett.slang.logging.fmtfatalerr
import com.sachett.slang.parser.SlangBaseListener
import com.sachett.slang.parser.SlangBaseVisitor
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.staticchecker.ExpressionTypeDetector
import com.sachett.slang.slangc.symbol.FunctionSymbol
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable

/**
 * Analyzes the possible return paths in a function and checks the return type.
 * Constructor arguments are detailed below.
 * @param   symbolTable
 * @param   functionSymbol          The FunctionSymbol corresponding to the function definition.
 * @param   functionBlockContext    The BlockContext of the function to analyze.
 * @param   funcReturnType          The return type expected in all the return paths.
 */
class FunctionBlockPathsBuilder(
    private val symbolTable: SymbolTable,
    private val functionSymbol: FunctionSymbol,
    private val functionBlockContext: SlangParser.BlockContext,
    private val funcReturnType: SymbolType = SymbolType.VOID
) : SlangBaseListener() {
    private val topMostBlock = FunctionInnerBlock(functionBlockContext)
    private var currentBlock = topMostBlock

    override fun enterBlock(ctx: SlangParser.BlockContext?) {
        val functionInnerBlock = FunctionInnerBlock(ctx!!, parentBlock = currentBlock)
        currentBlock.childrenBlocks.add(functionInnerBlock)
        currentBlock = functionInnerBlock
    }

    override fun enterReturnStmtNoExpr(ctx: SlangParser.ReturnStmtNoExprContext?) {
        if (currentBlock.hasReturn) {
            return
        }

        currentBlock.hasReturn = true
        currentBlock.returnsType = SymbolType.VOID
    }

    override fun enterReturnStmtWithExpr(ctx: SlangParser.ReturnStmtWithExprContext?) {
        if (currentBlock.hasReturn) {
            return
        }

        val expressionTypeDetector = ExpressionTypeDetector(symbolTable)
        val (homoTypes, exprReturnType) = expressionTypeDetector.getType(ctx!!.expr())

        if (!homoTypes) {
            fmtfatalerr(
                "The expression in the return statement has mixed types in it and cannot be evaluated.",
                ctx.RETURN().symbol.line
            )
        }

        currentBlock.hasReturn = true
        currentBlock.returnsType = exprReturnType
    }

    override fun exitBlock(ctx: SlangParser.BlockContext?) {
        if (currentBlock != topMostBlock) {
            currentBlock = currentBlock.parentBlock!!
        }
        super.exitBlock(ctx)
    }
}