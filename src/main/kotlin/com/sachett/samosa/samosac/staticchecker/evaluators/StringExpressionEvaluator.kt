package com.sachett.samosa.samosac.staticchecker.evaluators

import com.sachett.samosa.parser.SamosaBaseVisitor
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.symbol.SymbolType

import net.objecthunter.exp4j.ExpressionBuilder

class StringExpressionEvaluator(private var exprContext: SamosaParser.ExprContext): SamosaBaseVisitor<String>() {
    private var isExprStaticEvaluable = true
    private var isExprStaticEvaluableCalculated = false
    private var evaluationResult: String = ""
    /**
     * Checks if the expression can be evaluated at compile time.
     */
    public fun checkStaticEvaluable(): Boolean {
        if (isExprStaticEvaluableCalculated) {
            return isExprStaticEvaluable
        }

        evaluationResult = visit(exprContext)
        return isExprStaticEvaluable
    }

    /**
     * Evaluates and returns the integer value.
     * Returns default value of the symbol type if not possible to evaluate at compile time.
     */
    fun evaluate(): String {
        if (!checkStaticEvaluable()) {
            return SymbolType.STRING.defaultValue!! as String
        }

        return evaluationResult
    }

    fun setExprContext(exprContext: SamosaParser.ExprContext) {
        this.exprContext = exprContext
        isExprStaticEvaluableCalculated = false
        isExprStaticEvaluable = true
    }

    // only for checking purposes
    override fun visitExprIdentifier(ctx: SamosaParser.ExprIdentifierContext?): String {
        isExprStaticEvaluable = false
        return ""
    }

    override fun visitExprFunctionCall(ctx: SamosaParser.ExprFunctionCallContext?): String {
        isExprStaticEvaluable = false
        return ""
    }

    // for compile-time evaluation:
    override fun visitExprPlus(ctx: SamosaParser.ExprPlusContext?): String {
        return visit(ctx!!.expr(0)) + visit(ctx.expr(1))
    }

    override fun visitExprParen(ctx: SamosaParser.ExprParenContext?): String {
        return visit(ctx!!.expr())
    }

    override fun visitExprString(ctx: SamosaParser.ExprStringContext?): String? {
        val text = ctx!!.text

        if (text.length == 2 && text.equals("\"\"")) {
           return ""
        }

        return text.substring(1, text.length - 1)
    }
}