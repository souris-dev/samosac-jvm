package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.parser.SlangGrammarParser
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable

/**
 * Provides type checking methods for boolean expressions.
 */
class BoolExpressionChecker(symbolTable: SymbolTable) : ExpressionChecker(symbolTable) {

    /**
     * Type checks the provided boolean expression.
     * This checkExpr is an overload (and not an override), and should be used instead
     * of the superclass <code>ExpressionChecker</code>'s <code>checkExpr()</code> for checking
     * boolean expressions.
     * @param   ctx The <code>BooleanExprContext</code> to check.
     * @return  <code>true</code> if all OK else <code>false</code>.
     */
    fun checkExpr(ctx: SlangGrammarParser.BooleanExprContext): Boolean {
        return false
    }

    /* -----------------  Visitor methods -------------------- */

    override fun <T> checkUnaryOp(ctx: T): Boolean {
        return super.checkUnaryOp(ctx)
    }

    override fun <T> checkBinaryOp(ctx: T): Boolean {
        return super.checkBinaryOp(ctx)
    }

    override fun visitBooleanExprNot(ctx: SlangGrammarParser.BooleanExprNotContext?): Boolean {
        return super.visitBooleanExprNot(ctx)
    }

    override fun visitBooleanExprOr(ctx: SlangGrammarParser.BooleanExprOrContext?): Boolean {
        return super.visitBooleanExprOr(ctx)
    }

    override fun visitBooleanExprAnd(ctx: SlangGrammarParser.BooleanExprAndContext?): Boolean {
        return super.visitBooleanExprAnd(ctx)
    }

    override fun visitBooleanExprXor(ctx: SlangGrammarParser.BooleanExprXorContext?): Boolean {
        return super.visitBooleanExprXor(ctx)
    }

    override fun visitBooleanExprRelOp(ctx: SlangGrammarParser.BooleanExprRelOpContext?): Boolean {
        return super.visitBooleanExprRelOp(ctx)
    }

    override fun visitBooleanExprParen(ctx: SlangGrammarParser.BooleanExprParenContext?): Boolean {
        return super.visitBooleanExprParen(ctx)
    }

    override fun visitBooleanExprIdentifier(ctx: SlangGrammarParser.BooleanExprIdentifierContext?): Boolean {
        return super.visitBooleanExprIdentifier(ctx)
    }

    override fun visitBooleanTrue(ctx: SlangGrammarParser.BooleanTrueContext?): Boolean {
        return super.visitBooleanTrue(ctx)
    }

    override fun visitBooleanFalse(ctx: SlangGrammarParser.BooleanFalseContext?): Boolean {
        return super.visitBooleanFalse(ctx)
    }

    override fun visitBooleanFunctionCall(ctx: SlangGrammarParser.BooleanFunctionCallContext?): Boolean {
        return super.visitBooleanFunctionCall(ctx)
    }

    override fun visitFunctionCallWithArgs(ctx: SlangGrammarParser.FunctionCallWithArgsContext?): Boolean {
        return super.visitFunctionCallWithArgs(ctx)
    }

    override fun visitFunctionCallNoArgs(ctx: SlangGrammarParser.FunctionCallNoArgsContext?): Boolean {
        return super.visitFunctionCallNoArgs(ctx)
    }
}