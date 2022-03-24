package com.sachett.samosa.samosac.staticchecker

import com.sachett.samosa.logging.fmterror
import com.sachett.samosa.parser.SamosaBaseListener
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.symbol.FunctionSymbol
import com.sachett.samosa.samosac.symbol.SymbolType
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable
import org.antlr.v4.runtime.tree.ParseTreeWalker

/**
 * Checks the function body and return statements in the function body. Currently, it checks:
 * 1. Whether the return value is of the correct type
 * 2. Whether the function returns a value if it should be returning a value
 * 3. Whether there is a nested function inside.
 *
 * TODO: Add error for "not all control paths return a value"
 * Use the checkReturnStmts() function with the appropriate overload.
 */
class FunctionReturnsChecker(
    private val symbolTable: SymbolTable,
    private val fnSymbol: FunctionSymbol
) : SamosaBaseListener() {
    private var functionBlockEntered: Boolean = false
    private val mustHaveReturn = fnSymbol.returnType != SymbolType.VOID
    private var allOk = true
    private var foundReturn = false

    fun checkReturnStmts(ctx: SamosaParser.ImplicitRetTypeFuncDefContext): Boolean {
        println("###---- Checking returns -----") // DEBUG
        ParseTreeWalker.DEFAULT.walk(this, ctx)
        println("###----- Finished checking returns -----") // DEBUG
        return allOk
    }

    fun checkReturnStmts(ctx: SamosaParser.ExplicitRetTypeFuncDefContext): Boolean {
        println("###---- Checking returns -----") // DEBUG
        ParseTreeWalker.DEFAULT.walk(this, ctx)
        println("###----- Finished checking returns -----") // DEBUG
        return allOk
    }

    private fun checkFunInFun(fnDefinitionLineNum: Int) {
        if (!functionBlockEntered) {
            functionBlockEntered = true
        } else {
            fmterror("Functions within functions are not yet supported.", fnDefinitionLineNum)
            allOk = false
        }
    }

    private fun checkReturnedOrNot(fnDefinitionLineNum: Int) {
        if (mustHaveReturn && !foundReturn) {
            fmterror("Function must return a value of type ${fnSymbol.returnType.asString}.", fnDefinitionLineNum)
            allOk = false
        }
    }

    override fun enterImplicitRetTypeFuncDef(ctx: SamosaParser.ImplicitRetTypeFuncDefContext?) =
        checkFunInFun(ctx!!.IDENTIFIER().symbol.line)

    override fun enterExplicitRetTypeFuncDef(ctx: SamosaParser.ExplicitRetTypeFuncDefContext?) =
        checkFunInFun(ctx!!.IDENTIFIER().symbol.line)

    override fun exitImplicitRetTypeFuncDef(ctx: SamosaParser.ImplicitRetTypeFuncDefContext?) =
        checkReturnedOrNot(ctx!!.IDENTIFIER().symbol.line)

    override fun exitExplicitRetTypeFuncDef(ctx: SamosaParser.ExplicitRetTypeFuncDefContext?) =
        checkReturnedOrNot(ctx!!.IDENTIFIER().symbol.line)

    override fun enterReturnStmtNoExpr(ctx: SamosaParser.ReturnStmtNoExprContext?) {
        val lineNum = ctx!!.RETURN().symbol.line
        foundReturn = true

        if (fnSymbol.returnType != SymbolType.VOID) {
            fmterror(
                "Expected a return value of type ${fnSymbol.returnType.asString} from the function, " +
                        "but found return statement with no expression.",
                lineNum
            )
            allOk = false
        }
    }

    override fun enterReturnStmtWithExpr(ctx: SamosaParser.ReturnStmtWithExprContext?) {
        val lineNum = ctx!!.RETURN().symbol.line
        foundReturn = true

        println("Checking return at line ${lineNum}") // DEBUG
        val expressionTypeDetector = ExpressionTypeDetector(symbolTable)
        val (homoTypes, expType) = expressionTypeDetector.getType(ctx.expr())

        if (!homoTypes) {
            fmterror("Mismatched types in expression in return statement.", lineNum)
            allOk = false
        }

        if (expType != fnSymbol.returnType) {
            fmterror(
                "Expected return value of type ${fnSymbol.returnType.asString} " +
                        "but found expression of type ${expType.asString}",
                lineNum
            )
            allOk = false
        }
    }

    override fun enterBlock(ctx: SamosaParser.BlockContext?) {
        println("Inc scope in FunctionReturnsChecker") // DEBUG
        symbolTable.incrementScopeOverrideScopeCreation(false)
    }

    override fun exitBlock(ctx: SamosaParser.BlockContext?) {
        println("Dec scope in FunctionReturnsChecker") // DEBUG
        symbolTable.decrementScope()
    }
}