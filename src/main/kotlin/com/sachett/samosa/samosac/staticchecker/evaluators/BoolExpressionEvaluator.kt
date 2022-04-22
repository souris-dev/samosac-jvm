package com.sachett.samosa.samosac.staticchecker.evaluators

import com.sachett.samosa.logging.err
import com.sachett.samosa.parser.SamosaBaseVisitor
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.staticchecker.ExpressionTypeDetector
import com.sachett.samosa.samosac.symbol.SymbolType
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable

class BoolExpressionEvaluator(
    private var exprContext: SamosaParser.BooleanExprContext,
    private val symbolTable: SymbolTable
) : SamosaBaseVisitor<Boolean>(), IStaticExprEvaluator<Boolean> {
    private var isExprStaticEvaluable = true
    private var isExprStaticEvaluableCalculated = false
    private var evaluationResult: Boolean = false

    /**
     * Checks if the expression can be evaluated at compile time.
     */
    override fun checkStaticEvaluable(): Boolean {
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
    override fun evaluate(): Boolean {
        if (!checkStaticEvaluable()) {
            return SymbolType.BOOL.defaultValue!! as Boolean
        }

        return evaluationResult
    }
    
    fun setBooleanExprContext(booleanExprContext: SamosaParser.BooleanExprContext) {
        this.exprContext = booleanExprContext
        isExprStaticEvaluableCalculated = false
        isExprStaticEvaluable = true
        evaluationResult = false
    }

    override fun visitBooleanExprIdentifier(ctx: SamosaParser.BooleanExprIdentifierContext?): Boolean {
        isExprStaticEvaluable = false
        return false
    }

    override fun visitBooleanFunctionCall(ctx: SamosaParser.BooleanFunctionCallContext?): Boolean {
        isExprStaticEvaluable = false
        return false
    }

    override fun visitBooleanExprRelOp(ctx: SamosaParser.BooleanExprRelOpContext?): Boolean {
        var comparisonResult = false

        val lhs = ctx!!.expr(0)
        val rhs = ctx.expr(1)

        val typeDetector = ExpressionTypeDetector(symbolTable)
        val lhsType = typeDetector.getType(lhs)
        val rhsType = typeDetector.getType(rhs)

        // check for incompatible types
        if ((!lhsType.first || !rhsType.first)
            || (lhsType.second != rhsType.second)
            || !lhsType.second.canBeUsedWithRelOp
        ) {
            return false
        }

        val theRelOp = ctx.relOp()

        // RelOp expressions have two operands
        var leftVal: Any? = null
        var rightVal: Any? = null

        // Since we only have int expressions that can be compared using relops right now
        if (lhsType.second == SymbolType.INT && rhsType.second == SymbolType.INT) {
            val intExpressionEvaluator = IntExpressionEvaluator(ctx.expr(0))
            if (!intExpressionEvaluator.checkStaticEvaluable()) {
                isExprStaticEvaluable = false
                return false
            }
            leftVal = intExpressionEvaluator.evaluate()

            intExpressionEvaluator.setExprContext(ctx.expr(1))
            if (!intExpressionEvaluator.checkStaticEvaluable()) {
                isExprStaticEvaluable = false
                return false
            }
            rightVal = intExpressionEvaluator.evaluate()
        }

        if (theRelOp.GT() != null) {
            if (lhsType.second == SymbolType.INT && rhsType.second == SymbolType.INT) {
                comparisonResult = (leftVal!! as Int) > (rightVal!! as Int)
            }
        } else if (theRelOp.GTEQ() != null) {
            if (lhsType.second == SymbolType.INT && rhsType.second == SymbolType.INT) {
                comparisonResult = (leftVal!! as Int) >= (rightVal!! as Int)
            }
        } else if (theRelOp.LT() != null) {
            if (lhsType.second == SymbolType.INT && rhsType.second == SymbolType.INT) {
                comparisonResult = (leftVal!! as Int) < (rightVal!! as Int)
            }
        } else if (theRelOp.LTEQ() != null) {
            if (lhsType.second == SymbolType.INT && rhsType.second == SymbolType.INT) {
                comparisonResult = (leftVal!! as Int) <= (rightVal!! as Int)
            }
        } else {
            err("[Error] Unknown relational operator.")
        }

        return comparisonResult
    }

    override fun visitBooleanExprOr(ctx: SamosaParser.BooleanExprOrContext?): Boolean {
        val left = visit(ctx!!.booleanExpr(0)) // left operand
        val right = visit(ctx.booleanExpr(1)) // right operand

        return left || right
    }

    override fun visitBooleanExprCompOp(ctx: SamosaParser.BooleanExprCompOpContext?): Boolean {
        var comparisonResult = false

        val lhs = ctx!!.expr(0)
        val rhs = ctx.expr(1)

        val typeDetector = ExpressionTypeDetector(symbolTable)
        val lhsType = typeDetector.getType(lhs)
        val rhsType = typeDetector.getType(rhs)

        // check for incompatible types
        if ((!lhsType.first || !rhsType.first)
            || (lhsType.second != rhsType.second)
            || !lhsType.second.canBeUsedWithRelOp
        ) {
            return false
        }

        val theCompOp = ctx.compOp()

        // RelOp expressions have two operands
        var leftVal: Any? = null
        var rightVal: Any? = null

        // Currently, we can only compare strings and ints using == and !=
        if (lhsType.second == SymbolType.INT && rhsType.second == SymbolType.INT) {
            val intExpressionEvaluator = IntExpressionEvaluator(ctx.expr(0))
            if (!intExpressionEvaluator.checkStaticEvaluable()) {
                isExprStaticEvaluable = false
                return false
            }
            leftVal = intExpressionEvaluator.evaluate()

            intExpressionEvaluator.setExprContext(ctx.expr(1))
            if (!intExpressionEvaluator.checkStaticEvaluable()) {
                isExprStaticEvaluable = false
                return false
            }
            rightVal = intExpressionEvaluator.evaluate()
        } else if (lhsType.second == SymbolType.STRING && rhsType.second == SymbolType.STRING) {
            val stringExpressionEvaluator = StringExpressionEvaluator(ctx.expr(0))
            if (!stringExpressionEvaluator.checkStaticEvaluable()) {
                isExprStaticEvaluable = false
                return false
            }
            leftVal = stringExpressionEvaluator.evaluate()

            stringExpressionEvaluator.setExprContext(ctx.expr(1))
            if (!stringExpressionEvaluator.checkStaticEvaluable()) {
                isExprStaticEvaluable = false
                return false
            }
            rightVal = stringExpressionEvaluator.evaluate()
        }

        if (theCompOp.COMP() != null) {
            if (lhsType.second == SymbolType.INT && rhsType.second == SymbolType.INT) {
                comparisonResult = (leftVal!! as Int) == (rightVal!! as Int)
            } else if (lhsType.second == SymbolType.STRING && rhsType.second == SymbolType.STRING) {
                comparisonResult = (leftVal!! as String) == (rightVal!! as String)
            }
        } else if (theCompOp.COMPNOTEQ() != null) {
            if (lhsType.second == SymbolType.INT && rhsType.second == SymbolType.INT) {
                comparisonResult = (leftVal!! as Int) != (rightVal!! as Int)
            } else if (lhsType.second == SymbolType.STRING && rhsType.second == SymbolType.STRING) {
                comparisonResult = (leftVal!! as String) != (rightVal!! as String)
            }
        } else {
            err("[Error] Unknown comparison operator.")
        }

        return comparisonResult
    }

    override fun visitBooleanExprParen(ctx: SamosaParser.BooleanExprParenContext?): Boolean {
        return visit(ctx!!.booleanExpr())
    }

    override fun visitBooleanTrue(ctx: SamosaParser.BooleanTrueContext?): Boolean {
        return true
    }

    override fun visitBooleanFalse(ctx: SamosaParser.BooleanFalseContext?): Boolean {
        return false
    }

    override fun visitBooleanExprNot(ctx: SamosaParser.BooleanExprNotContext?): Boolean {
        return !(visit(ctx!!.booleanExpr()))
    }

    override fun visitBooleanExprXor(ctx: SamosaParser.BooleanExprXorContext?): Boolean {
        val left = visit(ctx!!.booleanExpr(0)) // left operand
        val right = visit(ctx.booleanExpr(1)) // right operand

        return ((left || right) && !(left && right))
    }

    override fun visitBooleanExprAnd(ctx: SamosaParser.BooleanExprAndContext?): Boolean {
        val left = visit(ctx!!.booleanExpr(0)) // left operand
        val right = visit(ctx.booleanExpr(1)) // right operand

        return left && right
    }
}