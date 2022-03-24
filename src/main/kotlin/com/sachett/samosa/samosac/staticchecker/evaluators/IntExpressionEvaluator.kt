package com.sachett.samosa.samosac.staticchecker.evaluators

import com.sachett.samosa.parser.SamosaBaseVisitor
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.symbol.SymbolType

import net.objecthunter.exp4j.ExpressionBuilder

class IntExpressionEvaluator(private var exprContext: SamosaParser.ExprContext): SamosaBaseVisitor<Void?>() {
    private var isExprStaticEvaluable = true
    private var isExprStaticEvaluableCalculated = false
    /**
     * Checks if the expression can be evaluated at compile time.
     */
    fun checkStaticEvaluable(): Boolean {
        if (isExprStaticEvaluableCalculated) {
            return isExprStaticEvaluable
        }

        visit(exprContext)
        return isExprStaticEvaluable
    }

    fun setExprContext(exprContext: SamosaParser.ExprContext) {
        this.exprContext = exprContext
        isExprStaticEvaluableCalculated = false
        isExprStaticEvaluable = true
    }

    /**
     * Evaluates and returns the integer value.
     * Returns default value of the symbol type if not possible to evaluate at compile time.
     */
    fun evaluate(): Int {
        if (!checkStaticEvaluable()) {
            return SymbolType.INT.defaultValue!! as Int
        }

        val exp = ExpressionBuilder(exprContext.text).build()
        return exp.evaluate().toInt()
    }

    override fun visitExprIdentifier(ctx: SamosaParser.ExprIdentifierContext?): Void? {
        isExprStaticEvaluable = false
        return super.visitExprIdentifier(ctx)
    }

    override fun visitExprFunctionCall(ctx: SamosaParser.ExprFunctionCallContext?): Void? {
        isExprStaticEvaluable = false
        return super.visitExprFunctionCall(ctx)
    }
}