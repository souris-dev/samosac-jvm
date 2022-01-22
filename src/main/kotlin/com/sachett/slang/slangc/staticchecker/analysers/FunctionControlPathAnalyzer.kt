package com.sachett.slang.slangc.staticchecker.analysers

import com.sachett.slang.parser.SlangBaseListener
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.staticchecker.ExpressionTypeDetector
import com.sachett.slang.slangc.symbol.FunctionSymbol
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.util.ArrayDeque

class FunctionControlPathAnalyzer(
    private val symbolTable: SymbolTable,
    private val fnSymbol: FunctionSymbol
) : SlangBaseListener() {

    fun checkAllControlPathsForReturns(ctx: SlangParser.ImplicitRetTypeFuncDefContext): Boolean {
        // No need to check if it returns void
        if (fnSymbol.returnType == SymbolType.VOID) {
            return true
        }

        ParseTreeWalker.DEFAULT.walk(this, ctx)

        return false
    }

    fun checkAllControlPathsForReturns(ctx: SlangParser.ExplicitRetTypeFuncDefContext): Boolean {
        // No need to check if it returns void
        if (fnSymbol.returnType == SymbolType.VOID) {
            return true
        }

        ParseTreeWalker.DEFAULT.walk(this, ctx)

        return false
    }

    /**
     * Build the tree for analyzing the function's control flow.
     */
    private var functionRootBlock = ControlBlock(fnSymbol, null, ControlBlockType.FUNCTIONROOT)
    private var currentStrayBlock: StrayBlock = functionRootBlock.children[0] as StrayBlock
    private var isInsideIfControlNode = false
    private var currentIfControlNode: IfControlNode? = null
    private var strayBlockQueue = ArrayDeque<StrayBlock>()

    override fun enterReturnStmtWithExpr(ctx: SlangParser.ReturnStmtWithExprContext?) {
        val expressionTypeDetector = ExpressionTypeDetector(symbolTable)
        val (homoTypes, expType) = expressionTypeDetector.getType(ctx!!.expr())

        if (!currentStrayBlock.doesReturnProperly) {
            currentStrayBlock.doesReturnProperly = homoTypes && expType == fnSymbol.returnType
        }
    }

    override fun enterReturnStmtNoExpr(ctx: SlangParser.ReturnStmtNoExprContext?) {
        if (!currentStrayBlock.doesReturnProperly) {
            currentStrayBlock.doesReturnProperly = fnSymbol.returnType == SymbolType.VOID
        }
    }

    override fun enterIfStmt(ctx: SlangParser.IfStmtContext?) {
        val ifControlNode = IfControlNode(fnSymbol, currentStrayBlock.parent!!, ctx!!)

        isInsideIfControlNode = true
        currentStrayBlock.parent?.children?.add(ifControlNode)
        currentIfControlNode = ifControlNode

        for (child in ifControlNode.children) {
            for (grandChild in child.children) {
                if (grandChild is StrayBlock) {
                    strayBlockQueue.addLast(grandChild)
                }
            }
        }
    }

    override fun exitIfStmt(ctx: SlangParser.IfStmtContext?) {
        super.exitIfStmt(ctx)
    }

    override fun enterBlock(ctx: SlangParser.BlockContext?) {
        currentStrayBlock = strayBlockQueue.removeFirst()
    }
}