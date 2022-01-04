package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.logging.fmterror
import com.sachett.slang.parser.SlangBaseListener
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.symbol.FunctionSymbol
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable
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
) : SlangBaseListener() {
    private var functionBlockEntered: Boolean = false
    private val mustHaveReturn = fnSymbol.returnType != SymbolType.VOID
    private var allOk = true
    private var foundReturn = false

    fun checkReturnStmts(ctx: SlangParser.ImplicitRetTypeFuncDefContext): Boolean {
        ParseTreeWalker.DEFAULT.walk(this, ctx)
        return allOk
    }

    fun checkReturnStmts(ctx: SlangParser.ExplicitRetTypeFuncDefContext): Boolean {
        ParseTreeWalker.DEFAULT.walk(this, ctx)
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

    override fun enterImplicitRetTypeFuncDef(ctx: SlangParser.ImplicitRetTypeFuncDefContext?) =
        checkFunInFun(ctx!!.IDENTIFIER().symbol.line)

    override fun enterExplicitRetTypeFuncDef(ctx: SlangParser.ExplicitRetTypeFuncDefContext?) =
        checkFunInFun(ctx!!.IDENTIFIER().symbol.line)

    override fun exitImplicitRetTypeFuncDef(ctx: SlangParser.ImplicitRetTypeFuncDefContext?) =
        checkReturnedOrNot(ctx!!.IDENTIFIER().symbol.line)

    override fun exitExplicitRetTypeFuncDef(ctx: SlangParser.ExplicitRetTypeFuncDefContext?) =
        checkReturnedOrNot(ctx!!.IDENTIFIER().symbol.line)

    override fun enterReturnStmtNoExpr(ctx: SlangParser.ReturnStmtNoExprContext?) {
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

    override fun enterReturnStmtWithExpr(ctx: SlangParser.ReturnStmtWithExprContext?) {
        val lineNum = ctx!!.RETURN().symbol.line
        foundReturn = true

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
}