package com.sachett.samosa.samosac.staticchecker.analyzers

import com.sachett.samosa.parser.SamosaBaseListener
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.staticchecker.ExpressionTypeDetector
import com.sachett.samosa.samosac.staticchecker.analyzers.blocks.*
import com.sachett.samosa.samosac.symbol.FunctionSymbol
import com.sachett.samosa.samosac.symbol.SymbolType
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.util.ArrayDeque

class FunctionControlPathAnalyzer(
    private val symbolTable: SymbolTable,
    private val fnSymbol: FunctionSymbol
) : SamosaBaseListener() {

    fun checkAllControlPathsForReturns(ctx: SamosaParser.ImplicitRetTypeFuncDefContext): Boolean {
        println("**--- Starting control paths analysis ---") // DEBUG
        // No need to check if it returns void
        if (fnSymbol.returnType == SymbolType.VOID) {
            return true
        }

        ParseTreeWalker.DEFAULT.walk(this, ctx)

        return functionRootBlock.doesReturnProperly
    }

    fun checkAllControlPathsForReturns(ctx: SamosaParser.ExplicitRetTypeFuncDefContext): Boolean {
        println("**--- Starting control paths analysis ---") // DEBUG
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
            isInsideControlNode = true
            currentControlNode = controlNodeStack.removeFirst()
        } else {
            isInsideControlNode = false
        }
    }

    override fun enterReturnStmtWithExpr(ctx: SamosaParser.ReturnStmtWithExprContext?) {
        val expressionTypeDetector = ExpressionTypeDetector(symbolTable)
        val (homoTypes, expType) = expressionTypeDetector.getType(ctx!!.expr())

        if (!currentStrayBlock.doesReturnProperly) {
            currentStrayBlock.doesReturnProperly = homoTypes && expType == fnSymbol.returnType
        }
    }

    override fun enterReturnStmtNoExpr(ctx: SamosaParser.ReturnStmtNoExprContext?) {
        if (!currentStrayBlock.doesReturnProperly) {
            currentStrayBlock.doesReturnProperly = fnSymbol.returnType == SymbolType.VOID
        }
    }

    override fun enterReturnStmtWithBooleanExpr(ctx: SamosaParser.ReturnStmtWithBooleanExprContext?) {
        if (!currentStrayBlock.doesReturnProperly) {
            currentStrayBlock.doesReturnProperly = fnSymbol.returnType == SymbolType.BOOL
        }
    }

    override fun enterIfStmt(ctx: SamosaParser.IfStmtContext?) {
        val ifControlNode = IfControlNode(fnSymbol, currentStrayBlock.parent!!, ctx!!)
        addControlNode(ifControlNode)
    }

    override fun exitIfStmt(ctx: SamosaParser.IfStmtContext?) {
        exitControlNode()
    }

    override fun enterWhileStmt(ctx: SamosaParser.WhileStmtContext?) {
        val whileControlNode = WhileControlNode(fnSymbol, currentStrayBlock.parent!!, ctx!!)
        addControlNode(whileControlNode)
    }

    override fun exitWhileStmt(ctx: SamosaParser.WhileStmtContext?) {
        exitControlNode()
    }

    override fun enterBlock(ctx: SamosaParser.BlockContext?) {
        symbolTable.incrementScopeOverrideScopeCreation(false)
        if (ctx!!.parent is SamosaParser.IfStmtContext
            || ctx.parent is SamosaParser.WhileStmtContext
            || ctx.parent is SamosaParser.ImplicitRetTypeFuncDefContext?
            || ctx.parent is SamosaParser.ExplicitRetTypeFuncDefContext?
        ) {
            currentStrayBlock = strayBlockQueue.removeFirst()
        }
    }

    override fun exitBlock(ctx: SamosaParser.BlockContext?) {
        symbolTable.decrementScope(false)
    }

    override fun visitErrorNode(node: ErrorNode?) {
        // TODO: Handle this
        super.visitErrorNode(node)
    }
}