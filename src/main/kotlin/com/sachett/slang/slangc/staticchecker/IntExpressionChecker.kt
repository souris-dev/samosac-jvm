package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.parser.SlangGrammarParser
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable

class IntExpressionChecker(symbolTable: SymbolTable) : ExpressionChecker(symbolTable) {
    override fun checkExpr(ctx: SlangGrammarParser.ExprContext): Boolean {
        return super.checkExpr(ctx)
    }

    override fun <T> checkBinaryOp(ctx: T): Boolean {
        return super.checkBinaryOp(ctx)
    }

    override fun <T> checkUnaryOp(ctx: T): Boolean {
        return super.checkUnaryOp(ctx)
    }

    /* --------------- Visitor methods -------------- */

    override fun visitUnaryMinus(ctx: SlangGrammarParser.UnaryMinusContext?): Boolean {
        return super.visitUnaryMinus(ctx)
    }

    override fun visitExprDivide(ctx: SlangGrammarParser.ExprDivideContext?): Boolean {
        return super.visitExprDivide(ctx)
    }

    override fun visitExprMultiply(ctx: SlangGrammarParser.ExprMultiplyContext?): Boolean {
        return super.visitExprMultiply(ctx)
    }

    override fun visitExprModulo(ctx: SlangGrammarParser.ExprModuloContext?): Boolean {
        return super.visitExprModulo(ctx)
    }

    override fun visitExprPlus(ctx: SlangGrammarParser.ExprPlusContext?): Boolean {
        return super.visitExprPlus(ctx)
    }

    override fun visitExprMinus(ctx: SlangGrammarParser.ExprMinusContext?): Boolean {
        return super.visitExprMinus(ctx)
    }

    override fun visitExprParen(ctx: SlangGrammarParser.ExprParenContext?): Boolean {
        return super.visitExprParen(ctx)
    }

    override fun visitExprIdentifier(ctx: SlangGrammarParser.ExprIdentifierContext?): Boolean {
        return super.visitExprIdentifier(ctx)
    }

    override fun visitFunctionCallNoArgs(ctx: SlangGrammarParser.FunctionCallNoArgsContext?): Boolean {
        return super.visitFunctionCallNoArgs(ctx)
    }

    override fun visitFunctionCallWithArgs(ctx: SlangGrammarParser.FunctionCallWithArgsContext?): Boolean {
        return super.visitFunctionCallWithArgs(ctx)
    }

    override fun visitExprDecint(ctx: SlangGrammarParser.ExprDecintContext?): Boolean {
        return super.visitExprDecint(ctx)
    }
}