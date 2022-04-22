package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.parser.SlangParser
import com.sachett.slang.parser.SlangBaseVisitor
import com.sachett.slang.slangc.symbol.SymbolType
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable

import com.sachett.slang.logging.err

abstract class ExpressionChecker(protected val symbolTable: SymbolTable) : SlangBaseVisitor<Boolean>() {

    /* This is the function that should be called from outside */
    open fun checkExpr(ctx: SlangParser.ExprContext): Boolean = false

    /* Check if the given unary operations and binary operations are valid in the expression. */
    protected open fun <T> checkUnaryOp(ctx: T) = false
    protected open fun <T> checkBinaryOp(ctx: T) = false

    /* Checks if the identifier in the expression is of the expected type */
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

        val symbol = symbolTable.lookup(idName) ?:
            err("[Error, Line $lineNumber] Unknown identifier $idName.")

        // if same type (as expected) return true
        if (symbol.isSymbolType(expectedType)) {
            return true
        }

        // else error out
        error(
            "[Error, Line $lineNumber] The identifier $idName has a type mismatch with the required type in " +
                    "the expression. The expected type was ${expectedType.asString} but the type found was " +
                    symbol.symbolType.asString + "."
        )
    }

    /* -----------------  Visitor methods -------------------- */

    override fun visitUnaryMinus(ctx: SlangParser.UnaryMinusContext?): Boolean = false
    override fun visitExprDivide(ctx: SlangParser.ExprDivideContext?): Boolean = false
    override fun visitExprMultiply(ctx: SlangParser.ExprMultiplyContext?): Boolean = false
    override fun visitExprModulo(ctx: SlangParser.ExprModuloContext?): Boolean = false
    override fun visitExprPlus(ctx: SlangParser.ExprPlusContext?): Boolean = false
    override fun visitExprMinus(ctx: SlangParser.ExprMinusContext?): Boolean = false
    override fun visitExprParen(ctx: SlangParser.ExprParenContext?): Boolean = false
    override fun visitExprIdentifier(ctx: SlangParser.ExprIdentifierContext?): Boolean = false
    override fun visitExprDecint(ctx: SlangParser.ExprDecintContext?): Boolean = false
    override fun visitExprString(ctx: SlangParser.ExprStringContext?): Boolean = false
    override fun visitBooleanExprNot(ctx: SlangParser.BooleanExprNotContext?): Boolean = false
    override fun visitBooleanExprOr(ctx: SlangParser.BooleanExprOrContext?): Boolean = false
    override fun visitBooleanExprAnd(ctx: SlangParser.BooleanExprAndContext?): Boolean = false
    override fun visitBooleanExprXor(ctx: SlangParser.BooleanExprXorContext?): Boolean = false
    override fun visitBooleanExprRelOp(ctx: SlangParser.BooleanExprRelOpContext?): Boolean = false
    override fun visitBooleanExprParen(ctx: SlangParser.BooleanExprParenContext?): Boolean = false
    override fun visitBooleanExprIdentifier(ctx: SlangParser.BooleanExprIdentifierContext?): Boolean = false
    override fun visitBooleanTrue(ctx: SlangParser.BooleanTrueContext?): Boolean = false
    override fun visitBooleanFalse(ctx: SlangParser.BooleanFalseContext?): Boolean = false
    override fun visitBooleanFunctionCall(ctx: SlangParser.BooleanFunctionCallContext?): Boolean = false
    override fun visitFunctionCallWithArgs(ctx: SlangParser.FunctionCallWithArgsContext?): Boolean = false
    override fun visitFunctionCallNoArgs(ctx: SlangParser.FunctionCallNoArgsContext?): Boolean = false
}