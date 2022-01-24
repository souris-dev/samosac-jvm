package com.sachett.slang.slangc.staticchecker.analyzers

import com.sachett.slang.parser.SlangBaseListener
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.staticchecker.ExpressionTypeDetector
import com.sachett.slang.slangc.symbol.FunctionSymbol
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable
import org.antlr.v4.runtime.tree.ErrorNode
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

        return functionRootBlock.doesReturnProperly
    }

    fun checkAllControlPathsForReturns(ctx: SlangParser.ExplicitRetTypeFuncDefContext): Boolean {
        // No need to check if it returns void
        if (fnSymbol.returnType == SymbolType.VOID) {
            return true
        }

        ParseTreeWalker.DEFAULT.walk(this, ctx)

        return functionRootBlock.doesReturnProperly
    }

    /**
     * Build the tree for analyzing the function's control flow.
     */
    private var functionRootBlock = ControlBlock(fnSymbol, null, ControlBlockType.FUNCTIONROOT)
    private var currentStrayBlock: StrayBlock = functionRootBlock.children[0] as StrayBlock
    private var isInsideIfControlNode = false
    private var currentIfControlNode: IfControlNode? = null
    private var strayBlockQueue = ArrayDeque<StrayBlock>(arrayListOf(currentStrayBlock))
    private var ifControlNodeStack = ArrayDeque<IfControlNode>()

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
        if (isInsideIfControlNode && currentIfControlNode != null) {
            ifControlNodeStack.addLast(currentIfControlNode!!)
        }

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
        // Even if this StrayBlock is extraneous
        // (that is, this if statement was the last statement inside the block),
        // it won't matter.
        val strayStrayBlock = StrayBlock(fnSymbol, doesReturnProperly = false, parent = currentIfControlNode?.parent)
        currentIfControlNode?.parent?.children?.add(strayStrayBlock)
        currentStrayBlock = strayStrayBlock

        if (!ifControlNodeStack.isEmpty()) {
            isInsideIfControlNode = true
            currentIfControlNode = ifControlNodeStack.removeFirst()
        } else {
            isInsideIfControlNode = false
        }

        super.exitIfStmt(ctx)
    }

    override fun enterBlock(ctx: SlangParser.BlockContext?) {
        if (ctx!!.parent is SlangParser.IfStmtContext
            || ctx.parent is SlangParser.ImplicitRetTypeFuncDefContext?
            || ctx.parent is SlangParser.ExplicitRetTypeFuncDefContext?
        ) {
            currentStrayBlock = strayBlockQueue.removeFirst()
        }
    }

    override fun visitErrorNode(node: ErrorNode?) {
        // TODO: Handle this
        super.visitErrorNode(node)
    }
}