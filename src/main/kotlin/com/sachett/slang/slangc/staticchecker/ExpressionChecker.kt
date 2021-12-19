package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.parser.SlangGrammarParser
import com.sachett.slang.parser.SlangGrammarBaseVisitor
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable

abstract class ExpressionChecker(protected val symbolTable: SymbolTable) : SlangGrammarBaseVisitor<Boolean>() {
    override fun visitUnaryMinus(ctx: SlangGrammarParser.UnaryMinusContext?): Boolean = false
    override fun visitExprDivide(ctx: SlangGrammarParser.ExprDivideContext?): Boolean = false
    override fun visitExprMultiply(ctx: SlangGrammarParser.ExprMultiplyContext?): Boolean = false
    override fun visitExprModulo(ctx: SlangGrammarParser.ExprModuloContext?): Boolean = false
    override fun visitExprPlus(ctx: SlangGrammarParser.ExprPlusContext?): Boolean = false
    override fun visitExprMinus(ctx: SlangGrammarParser.ExprMinusContext?): Boolean = false
    override fun visitExprParen(ctx: SlangGrammarParser.ExprParenContext?): Boolean = false
    override fun visitExprIdentifier(ctx: SlangGrammarParser.ExprIdentifierContext?): Boolean = false
    override fun visitExprDecint(ctx: SlangGrammarParser.ExprDecintContext?): Boolean = false
    override fun visitExprString(ctx: SlangGrammarParser.ExprStringContext?): Boolean = false
    override fun visitBooleanExprNot(ctx: SlangGrammarParser.BooleanExprNotContext?): Boolean = false
    override fun visitBooleanExprOr(ctx: SlangGrammarParser.BooleanExprOrContext?): Boolean = false
    override fun visitBooleanExprAnd(ctx: SlangGrammarParser.BooleanExprAndContext?): Boolean = false
    override fun visitBooleanExprXor(ctx: SlangGrammarParser.BooleanExprXorContext?): Boolean = false
    override fun visitBooleanExprRelOp(ctx: SlangGrammarParser.BooleanExprRelOpContext?): Boolean = false
    override fun visitBooleanExprParen(ctx: SlangGrammarParser.BooleanExprParenContext?): Boolean = false
    override fun visitBooleanExprIdentifier(ctx: SlangGrammarParser.BooleanExprIdentifierContext?): Boolean = false
    override fun visitBooleanTrue(ctx: SlangGrammarParser.BooleanTrueContext?): Boolean = false
    override fun visitBooleanFalse(ctx: SlangGrammarParser.BooleanFalseContext?): Boolean = false
    override fun visitBooleanFunctionCall(ctx: SlangGrammarParser.BooleanFunctionCallContext?): Boolean = false
    override fun visitFunctionCallWithArgs(ctx: SlangGrammarParser.FunctionCallWithArgsContext?): Boolean = false
    override fun visitFunctionCallNoArgs(ctx: SlangGrammarParser.FunctionCallNoArgsContext?): Boolean = false

    fun checkExpr(ctx: SlangGrammarParser.ExprContext): Boolean = false

    protected fun <T : Any> checkIdentifierTypeInExpr(
        ctx: T,
        expectedType: SymbolType
    ): Boolean {
        /* Use the java reflection API to get the identifier name and the line number */

        val ctxClass = ctx::class.java
        val identifier = ctxClass.getDeclaredMethod("IDENTIFIER")
        val identifierRetObj = identifier.invoke(ctx)
        val identifierMtdRetClass = identifierRetObj::class.java

        val getText = identifierMtdRetClass.getDeclaredMethod("getText")
        val getSymbol = identifierMtdRetClass.getDeclaredMethod("getSymbol")

        val getSymbolRetObj = getSymbol.invoke(identifierRetObj)
        val getSymbolMtdRetClass = getSymbolRetObj::class.java
        val getLine = getSymbolMtdRetClass.getMethod("getLine")

        /* Retrieve the identifier name and line number */
        val idName = getText.invoke(identifierRetObj) as String
        val lineNumber = getLine.invoke(getSymbolRetObj) as Int

        val symbol = symbolTable.lookup(idName) ?: error("[Error, Line $lineNumber] Unknown identifier $idName.")

        // if same type (as expected) return true
        if (symbol.isSymbolType(expectedType)) {
            return true
        }

        // else error out
        error("[Error, Line $lineNumber] The identifier $idName has a type mismatch with the required type in " +
                "the expression. The expected type was ${expectedType.asString} but the type found was " +
                symbol.symbolType.asString + ".")
    }
}