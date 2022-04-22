package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.logging.Severity
import com.sachett.slang.logging.fmterror
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable

class StringExpressionChecker(symbolTable: SymbolTable) : ExpressionChecker(symbolTable) {
    override fun checkExpr(ctx: SlangParser.ExprContext): Boolean = visit(ctx)

    /* ------------- Visitor methods --------------- */

    override fun visitExprIdentifier(ctx: SlangParser.ExprIdentifierContext?): Boolean =
        checkIdentifierTypeInExpr(ctx!!, SymbolType.STRING)

    override fun visitExprString(ctx: SlangParser.ExprStringContext?): Boolean = true

    override fun visitExprPlus(ctx: SlangParser.ExprPlusContext?): Boolean = true

    override fun visitExprParen(ctx: SlangParser.ExprParenContext?): Boolean = true

    override fun visitFunctionCallNoArgs(ctx: SlangParser.FunctionCallNoArgsContext?): Boolean {
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

    override fun visitFunctionCallWithArgs(ctx: SlangParser.FunctionCallWithArgsContext?): Boolean {
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