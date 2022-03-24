package com.sachett.samosa.samosac.staticchecker

import com.sachett.samosa.logging.Severity
import com.sachett.samosa.logging.fmterror
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.symbol.SymbolType
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable

class IntExpressionChecker(symbolTable: SymbolTable) : ExpressionChecker(symbolTable) {
    override fun checkExpr(ctx: SamosaParser.ExprContext): Boolean = visit(ctx)

    override fun <T> checkUnaryOp(ctx: T): Boolean {
        // use java reflection API
        // this would be: visit(ctx?.booleanExpr())
        val ctxClass = ctx!!::class.java
        val expr = ctxClass.getDeclaredMethod("expr")
        val exprRetObj = expr.invoke(ctx) as SamosaParser.ExprContext

        return visit(exprRetObj)
    }

    override fun <T> checkBinaryOp(ctx: T): Boolean {
        // use java reflection API
        // this would be: visit(ctx?.booleanExpr(0)) && visit(ctx?.booleanExpr(1))
        val ctxClass = ctx!!::class.java
        val expr = ctxClass.getDeclaredMethod("expr", Int::class.java)
        val exprRetObjLHS = expr.invoke(ctx, 0) as SamosaParser.ExprContext
        val exprRetObjRHS = expr.invoke(ctx, 1) as SamosaParser.ExprContext

        return (visit(exprRetObjLHS) && visit(exprRetObjRHS))
    }

    /* --------------- Visitor methods -------------- */

    override fun visitUnaryMinus(ctx: SamosaParser.UnaryMinusContext?): Boolean = checkUnaryOp(ctx!!)

    override fun visitExprDivide(ctx: SamosaParser.ExprDivideContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprMultiply(ctx: SamosaParser.ExprMultiplyContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprModulo(ctx: SamosaParser.ExprModuloContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprPlus(ctx: SamosaParser.ExprPlusContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprMinus(ctx: SamosaParser.ExprMinusContext?): Boolean = checkBinaryOp(ctx!!)

    override fun visitExprParen(ctx: SamosaParser.ExprParenContext?): Boolean = checkUnaryOp(ctx!!)

    override fun visitExprIdentifier(ctx: SamosaParser.ExprIdentifierContext?): Boolean =
        checkIdentifierTypeInExpr(ctx!!, SymbolType.INT)

    override fun visitFunctionCallNoArgs(ctx: SamosaParser.FunctionCallNoArgsContext?): Boolean {
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

    override fun visitFunctionCallWithArgs(ctx: SamosaParser.FunctionCallWithArgsContext?): Boolean {
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

    override fun visitExprDecint(ctx: SamosaParser.ExprDecintContext?): Boolean = true
}