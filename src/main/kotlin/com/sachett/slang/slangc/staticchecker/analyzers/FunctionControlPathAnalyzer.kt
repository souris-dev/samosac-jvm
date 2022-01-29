package com.sachett.slang.slangc.staticchecker.analyzers

import com.sachett.slang.parser.SlangBaseListener
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.staticchecker.ExpressionTypeDetector
import com.sachett.slang.slangc.staticchecker.analyzers.blocks.*
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
    private var isInsideControlNode = false
    private var currentControlNode: ControlNode? = null
    private var strayBlockQueue = ArrayDeque<StrayBlock>(arrayListOf(currentStrayBlock))
    private var controlNodeStack = ArrayDeque<ControlNode>()

    private fun addControlNode(controlNode: ControlNode) {
        // first save the currentControlNode onto the stack
        if (isInsideControlNode && currentControlNode != null) {
            controlNodeStack.addLast(currentControlNode!!)
        }

        isInsideControlNode = true
        currentStrayBlock.parent?.children?.add(controlNode)
        currentControlNode = controlNode

        // any stray blocks inside controlNode should be added to the strayBlockQueue
        // so that upcoming blocks' statements are considered to be a part of them
        for (child in controlNode.children) {
            for (grandChild in child.children) {
                if (grandChild is StrayBlock) {
                    strayBlockQueue.addLast(grandChild)
                }
            }
        }
    }

    private fun exitControlNode() {
        val strayStrayBlock = StrayBlock(fnSymbol, doesReturnProperly = false, parent = currentControlNode?.parent)
        // Even if this StrayBlock is extraneous
        // (that is, this if statement was the last statement inside the block),
        // it won't matter.

        currentControlNode?.parent?.children?.add(strayStrayBlock)
        currentStrayBlock = strayStrayBlock

        if (!controlNodeStack.isEmpty()) {
            if (controlNodeStack.peekFirst() is IfControlNode) {
                isInsideControlNode = true
                currentControlNode = controlNodeStack.removeFirst() as IfControlNode
            }
        } else {
            isInsideControlNode = false
        }
    }

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
        addControlNode(ifControlNode)
    }

    override fun exitIfStmt(ctx: SlangParser.IfStmtContext?) {
        exitControlNode()
    }

    override fun enterWhileStmt(ctx: SlangParser.WhileStmtContext?) {
        val whileControlNode = WhileControlNode(fnSymbol, currentStrayBlock.parent!!, ctx!!)
        addControlNode(whileControlNode)
    }

    override fun exitWhileStmt(ctx: SlangParser.WhileStmtContext?) {
        exitControlNode()
    }

    override fun enterBlock(ctx: SlangParser.BlockContext?) {
        if (ctx!!.parent is SlangParser.IfStmtContext
            || ctx.parent is SlangParser.WhileStmtContext
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