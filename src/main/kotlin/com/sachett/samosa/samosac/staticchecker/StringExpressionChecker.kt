package com.sachett.samosa.samosac.staticchecker

import com.sachett.samosa.logging.Severity
import com.sachett.samosa.logging.fmterror
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.symbol.SymbolType
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable

class StringExpressionChecker(symbolTable: SymbolTable) : ExpressionChecker(symbolTable) {
    override fun checkExpr(ctx: SamosaParser.ExprContext): Boolean = visit(ctx)

    /* ------------- Visitor methods --------------- */

    override fun visitExprIdentifier(ctx: SamosaParser.ExprIdentifierContext?): Boolean =
        checkIdentifierTypeInExpr(ctx!!, SymbolType.STRING)

    override fun visitExprString(ctx: SamosaParser.ExprStringContext?): Boolean = true

    override fun visitExprPlus(ctx: SamosaParser.ExprPlusContext?): Boolean = true

    override fun visitExprParen(ctx: SamosaParser.ExprParenContext?): Boolean = true

    override fun visitFunctionCallNoArgs(ctx: SamosaParser.FunctionCallNoArgsContext?): Boolean {
        val returnSymbolType = FunctionCallExprChecker.getRetTypeOfFunctionCallNoArgs(ctx, symbolTable)
        if (returnSymbolType != SymbolType.STRING) {
            fmterror(
                "Expected return type was ${SymbolType.STRING.asString} but the function call returns " +
                        "value of type ${returnSymbolType.asString}.", ctx!!.IDENTIFIER().symbol.line, Severity.ERROR
            )
            return false
        }

        return true
    }

    override fun visitFunctionCallWithArgs(ctx: SamosaParser.FunctionCallWithArgsContext?): Boolean {
        val returnSymbolType = FunctionCallExprChecker.getRetTypeOfFunctionCallWithArgs(ctx, symbolTable)
        if (returnSymbolType != SymbolType.STRING) {
            fmterror(
                "Expected return type was ${SymbolType.STRING.asString} but the function call returns " +
                        "value of type ${returnSymbolType.asString}.", ctx!!.IDENTIFIER().symbol.line, Severity.ERROR
            )
            return false
        }

        return true
    }
}