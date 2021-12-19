package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.logging.Severity
import com.sachett.slang.logging.fmterror
import com.sachett.slang.parser.SlangGrammarParser
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable

class StringExpressionChecker(symbolTable: SymbolTable) : ExpressionChecker(symbolTable) {
    override fun checkExpr(ctx: SlangGrammarParser.ExprContext): Boolean = visit(ctx)

    /* ------------- Visitor methods --------------- */

    override fun visitExprIdentifier(ctx: SlangGrammarParser.ExprIdentifierContext?): Boolean =
        checkIdentifierTypeInExpr(ctx!!, SymbolType.STRING)

    override fun visitExprString(ctx: SlangGrammarParser.ExprStringContext?): Boolean = true

    override fun visitExprPlus(ctx: SlangGrammarParser.ExprPlusContext?): Boolean = false

    override fun visitFunctionCallNoArgs(ctx: SlangGrammarParser.FunctionCallNoArgsContext?): Boolean {
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

    override fun visitFunctionCallWithArgs(ctx: SlangGrammarParser.FunctionCallWithArgsContext?): Boolean {
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