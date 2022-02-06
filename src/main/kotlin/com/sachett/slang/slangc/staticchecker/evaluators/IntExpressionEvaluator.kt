package com.sachett.slang.slangc.staticchecker.evaluators

import com.sachett.slang.parser.SlangBaseVisitor
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.symbol.SymbolType

import net.objecthunter.exp4j.ExpressionBuilder

class IntExpressionEvaluator(private val exprContext: SlangParser.ExprContext): SlangBaseVisitor<Void?>() {
    private var isExprStaticEvaluable = true
    private var isExprStaticEvaluableCalculated = false
    /**
     * Checks if the expression can be evaluated at compile time.
     */
    public fun checkStaticEvaluable(): Boolean {
        if (isExprStaticEvaluableCalculated) {
            return isExprStaticEvaluable
        }

        visit(exprContext)
        return isExprStaticEvaluable
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

    override fun visitExprIdentifier(ctx: SlangParser.ExprIdentifierContext?): Void? {
        isExprStaticEvaluable = false
        return super.visitExprIdentifier(ctx)
    }

    override fun visitExprFunctionCall(ctx: SlangParser.ExprFunctionCallContext?): Void? {
        isExprStaticEvaluable = false
        return super.visitExprFunctionCall(ctx)
    }
}