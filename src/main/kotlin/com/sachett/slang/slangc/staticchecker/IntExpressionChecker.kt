package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.logging.Severity
import com.sachett.slang.logging.fmterror
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable

class IntExpressionChecker(symbolTable: SymbolTable) : ExpressionChecker(symbolTable) {
    override fun checkExpr(ctx: SlangParser.ExprContext): Boolean = visit(ctx)

    override fun <T> checkUnaryOp(ctx: T): Boolean {
        // use java reflection API
        // this would be: visit(ctx?.booleanExpr())
        val ctxClass = ctx!!::class.java
        val expr = ctxClass.getDeclaredMethod("expr")
        val exprRetObj = expr.invoke(ctx) as SlangParser.ExprContext

        return visit(exprRetObj)
    }

    override fun <T> checkBinaryOp(ctx: T): Boolean {
        // use java reflection API
        // this would be: visit(ctx?.booleanExpr(0)) && visit(ctx?.booleanExpr(1))
        val ctxClass = ctx!!::class.java
        val expr = ctxClass.getDeclaredMethod("expr", Int::class.java)
        val exprRetObjLHS = expr.invoke(ctx, 0) as SlangParser.ExprContext
        val exprRetObjRHS = expr.invoke(ctx, 1) as SlangParser.ExprContext

        return (visit(exprRetObjLHS) && visit(exprRetObjRHS))
    }

    /* --------------- Visitor methods -------------- */

    override fun visitUnaryMinus(ctx: SlangParser.UnaryMinusContext?): Boolean = checkUnaryOp(ctx!!)

    override fun visitExprDivide(ctx: SlangParser.ExprDivideContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprMultiply(ctx: SlangParser.ExprMultiplyContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprModulo(ctx: SlangParser.ExprModuloContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprPlus(ctx: SlangParser.ExprPlusContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprMinus(ctx: SlangParser.ExprMinusContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprParen(ctx: SlangParser.ExprParenContext?): Boolean = checkUnaryOp(ctx!!)

    override fun visitExprIdentifier(ctx: SlangParser.ExprIdentifierContext?): Boolean =
        checkIdentifierTypeInExpr(ctx!!, SymbolType.INT)

    override fun visitFunctionCallNoArgs(ctx: SlangParser.FunctionCallNoArgsContext?): Boolean {
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

    override fun visitFunctionCallWithArgs(ctx: SlangParser.FunctionCallWithArgsContext?): Boolean {
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

    override fun visitExprDecint(ctx: SlangParser.ExprDecintContext?): Boolean = true
}