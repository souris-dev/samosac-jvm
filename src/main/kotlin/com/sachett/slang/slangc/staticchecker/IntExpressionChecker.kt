package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.logging.Severity
import com.sachett.slang.logging.fmterror
import com.sachett.slang.parser.SlangGrammarParser
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable

class IntExpressionChecker(symbolTable: SymbolTable) : ExpressionChecker(symbolTable) {
    override fun checkExpr(ctx: SlangGrammarParser.ExprContext): Boolean = visit(ctx)

    override fun <T> checkUnaryOp(ctx: T): Boolean {
        // use java reflection API
        // this would be: visit(ctx?.booleanExpr())
        val ctxClass = ctx!!::class.java
        val expr = ctxClass.getDeclaredMethod("expr")
        val exprRetObj = expr.invoke(ctx) as SlangGrammarParser.ExprContext

        return visit(exprRetObj)
    }

    override fun <T> checkBinaryOp(ctx: T): Boolean {
        // use java reflection API
        // this would be: visit(ctx?.booleanExpr(0)) && visit(ctx?.booleanExpr(1))
        val ctxClass = ctx!!::class.java
        val expr = ctxClass.getDeclaredMethod("expr", Int::class.java)
        val exprRetObjLHS = expr.invoke(ctx, 0) as SlangGrammarParser.ExprContext
        val exprRetObjRHS = expr.invoke(ctx, 1) as SlangGrammarParser.ExprContext

        return (visit(exprRetObjLHS) && visit(exprRetObjRHS))
    }

    /* --------------- Visitor methods -------------- */

    override fun visitUnaryMinus(ctx: SlangGrammarParser.UnaryMinusContext?): Boolean = checkUnaryOp(ctx!!)

    override fun visitExprDivide(ctx: SlangGrammarParser.ExprDivideContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprMultiply(ctx: SlangGrammarParser.ExprMultiplyContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprModulo(ctx: SlangGrammarParser.ExprModuloContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprPlus(ctx: SlangGrammarParser.ExprPlusContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprMinus(ctx: SlangGrammarParser.ExprMinusContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprParen(ctx: SlangGrammarParser.ExprParenContext?): Boolean = checkUnaryOp(ctx!!)

    override fun visitExprIdentifier(ctx: SlangGrammarParser.ExprIdentifierContext?): Boolean =
        checkIdentifierTypeInExpr(ctx!!, SymbolType.INT)

    override fun visitFunctionCallNoArgs(ctx: SlangGrammarParser.FunctionCallNoArgsContext?): Boolean {
        val returnSymbolType = FunctionCallExprChecker.getRetTypeOfFunctionCallNoArgs(ctx, symbolTable)
        if (returnSymbolType != SymbolType.INT) {
            fmterror(
                "Expected return type was ${SymbolType.INT.asString} but the function call returns " +
                        "value of type ${returnSymbolType.asString}.", ctx!!.IDENTIFIER().symbol.line, Severity.ERROR
            )
            return false
        }

        return true
    }

    override fun visitFunctionCallWithArgs(ctx: SlangGrammarParser.FunctionCallWithArgsContext?): Boolean {
        val returnSymbolType = FunctionCallExprChecker.getRetTypeOfFunctionCallWithArgs(ctx, symbolTable)
        if (returnSymbolType != SymbolType.INT) {
            fmterror(
                "Expected return type was ${SymbolType.INT.asString} but the function call returns " +
                        "value of type ${returnSymbolType.asString}.", ctx!!.IDENTIFIER().symbol.line, Severity.ERROR
            )
            return false
        }

        return true
    }

    override fun visitExprDecint(ctx: SlangGrammarParser.ExprDecintContext?): Boolean = true
}