package com.sachett.samosa.samosac.staticchecker

import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.parser.SamosaBaseVisitor
import com.sachett.samosa.samosac.symbol.SymbolType
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable

import com.sachett.samosa.logging.err

abstract class ExpressionChecker(protected val symbolTable: SymbolTable) : SamosaBaseVisitor<Boolean>() {

    /* This is the function that should be called from outside */
    open fun checkExpr(ctx: SamosaParser.ExprContext): Boolean = false

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

    override fun visitUnaryMinus(ctx: SamosaParser.UnaryMinusContext?): Boolean = false
    override fun visitExprDivide(ctx: SamosaParser.ExprDivideContext?): Boolean = false
    override fun visitExprMultiply(ctx: SamosaParser.ExprMultiplyContext?): Boolean = false
    override fun visitExprModulo(ctx: SamosaParser.ExprModuloContext?): Boolean = false
    override fun visitExprPlus(ctx: SamosaParser.ExprPlusContext?): Boolean = false
    override fun visitExprMinus(ctx: SamosaParser.ExprMinusContext?): Boolean = false
    override fun visitExprParen(ctx: SamosaParser.ExprParenContext?): Boolean = false
    override fun visitExprIdentifier(ctx: SamosaParser.ExprIdentifierContext?): Boolean = false
    override fun visitExprDecint(ctx: SamosaParser.ExprDecintContext?): Boolean = false
    override fun visitExprString(ctx: SamosaParser.ExprStringContext?): Boolean = false
    override fun visitBooleanExprNot(ctx: SamosaParser.BooleanExprNotContext?): Boolean = false
    override fun visitBooleanExprOr(ctx: SamosaParser.BooleanExprOrContext?): Boolean = false
    override fun visitBooleanExprAnd(ctx: SamosaParser.BooleanExprAndContext?): Boolean = false
    override fun visitBooleanExprXor(ctx: SamosaParser.BooleanExprXorContext?): Boolean = false
    override fun visitBooleanExprRelOp(ctx: SamosaParser.BooleanExprRelOpContext?): Boolean = false
    override fun visitBooleanExprParen(ctx: SamosaParser.BooleanExprParenContext?): Boolean = false
    override fun visitBooleanExprIdentifier(ctx: SamosaParser.BooleanExprIdentifierContext?): Boolean = false
    override fun visitBooleanTrue(ctx: SamosaParser.BooleanTrueContext?): Boolean = false
    override fun visitBooleanFalse(ctx: SamosaParser.BooleanFalseContext?): Boolean = false
    override fun visitBooleanFunctionCall(ctx: SamosaParser.BooleanFunctionCallContext?): Boolean = false
    override fun visitFunctionCallWithArgs(ctx: SamosaParser.FunctionCallWithArgsContext?): Boolean = false
    override fun visitFunctionCallNoArgs(ctx: SamosaParser.FunctionCallNoArgsContext?): Boolean = false
}