package com.sachett.slang.slangc.staticchecker.evaluators

import com.sachett.slang.parser.SlangBaseVisitor
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.symbol.SymbolType

import net.objecthunter.exp4j.ExpressionBuilder

class StringExpressionEvaluator(private var exprContext: SlangParser.ExprContext): SlangBaseVisitor<String>() {
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

    fun setExprContext(exprContext: SlangParser.ExprContext) {
        this.exprContext = exprContext
        isExprStaticEvaluableCalculated = false
        isExprStaticEvaluable = true
    }

    // only for checking purposes
    override fun visitExprIdentifier(ctx: SlangParser.ExprIdentifierContext?): String {
        isExprStaticEvaluable = false
        return ""
    }

    override fun visitExprFunctionCall(ctx: SlangParser.ExprFunctionCallContext?): String {
        isExprStaticEvaluable = false
        return ""
    }

    // for compile-time evaluation:
    override fun visitExprPlus(ctx: SlangParser.ExprPlusContext?): String {
        return visit(ctx!!.expr(0)) + visit(ctx.expr(1))
    }

    override fun visitExprParen(ctx: SlangParser.ExprParenContext?): String {
        return visit(ctx!!.expr())
    }

    override fun visitExprString(ctx: SlangParser.ExprStringContext?): String? {
        val text = ctx!!.text

        if (text.length == 2 && text.equals("\"\"")) {
           return ""
        }

        return text.substring(1, text.length - 1)
    }
}