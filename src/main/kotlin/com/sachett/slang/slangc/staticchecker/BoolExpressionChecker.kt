package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.logging.Severity
import com.sachett.slang.logging.err
import com.sachett.slang.logging.fmterror
import com.sachett.slang.parser.SlangGrammarParser
import com.sachett.slang.slangc.symbol.SymbolType
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
        return visit(ctx)
    }

    /* -----------------  Visitor methods -------------------- */

    override fun <T> checkUnaryOp(ctx: T): Boolean {
        // use java reflection API
        // this would be: visit(ctx?.booleanExpr())
        val ctxClass = ctx!!::class.java
        val booleanExpr = ctxClass.getDeclaredMethod("booleanExpr")
        val booleanExprRetObj = booleanExpr.invoke(ctx) as SlangGrammarParser.BooleanExprContext

        return visit(booleanExprRetObj)
    }

    override fun <T> checkBinaryOp(ctx: T): Boolean {
        // use java reflection API
        // this would be: visit(ctx?.booleanExpr(0)) && visit(ctx?.booleanExpr(1))
        val ctxClass = ctx!!::class.java
        val booleanExpr = ctxClass.getDeclaredMethod("booleanExpr", Int::class.java)
        val booleanExprRetObjLHS = booleanExpr.invoke(ctx, 0) as SlangGrammarParser.BooleanExprContext
        val booleanExprRetObjRHS = booleanExpr.invoke(ctx, 1) as SlangGrammarParser.BooleanExprContext

        return (visit(booleanExprRetObjLHS) && visit(booleanExprRetObjRHS))
    }

    override fun visitBooleanExprNot(ctx: SlangGrammarParser.BooleanExprNotContext?): Boolean {
        return checkUnaryOp(ctx!!)
    }

    override fun visitBooleanExprOr(ctx: SlangGrammarParser.BooleanExprOrContext?): Boolean {
        return checkUnaryOp(ctx!!)
    }

    override fun visitBooleanExprAnd(ctx: SlangGrammarParser.BooleanExprAndContext?): Boolean {
        return checkUnaryOp(ctx!!)
    }

    override fun visitBooleanExprXor(ctx: SlangGrammarParser.BooleanExprXorContext?): Boolean {
        return checkUnaryOp(ctx!!)
    }

    override fun visitBooleanExprRelOp(ctx: SlangGrammarParser.BooleanExprRelOpContext?): Boolean {
        val lhs = ctx!!.expr(0)
        val rhs = ctx.expr(1)

        val typeDetector = ExpressionTypeDetector(symbolTable)
        val lhsType = typeDetector.getType(lhs)
        val rhsType = typeDetector.getType(rhs)

        // utility part to retrieve the line number and relop representation as string
        // for showing errors if required
        val lineNum: Int
        val op: String
        val theRelOp = ctx.relOp()

        if (theRelOp.GT() != null) {
            val gtOp = theRelOp.GT()!!.symbol!!
            lineNum = gtOp.line
            op = gtOp.text
        } else if (theRelOp.GTEQ() != null) {
            val gtEqOp = theRelOp.GTEQ()!!.symbol!!
            lineNum = gtEqOp.line
            op = gtEqOp.text
        } else if (theRelOp.LT() != null) {
            val ltOp = theRelOp.LT()!!.symbol!!
            lineNum = ltOp.line
            op = ltOp.text
        } else if (theRelOp.LTEQ() != null) {
            val ltEqOp = theRelOp.LTEQ()!!.symbol!!
            lineNum = ltEqOp.line
            op = ltEqOp.text
        } else {
            err("[Error] Unknown relational operator.")
        }

        // check if the LHS and RHS individually have homogenous types within
        if (!lhsType.first || !rhsType.first) {
            fmterror(
                "The expression on ${if (!lhsType.first) "LHS" else "RHS"} " +
                        "${if (!rhsType.first) "and RHS" else ""} " +
                        "has mixed types in it and cannot be evaluated.", lineNum, Severity.ERROR
            )
            return false
        }

        // note that the ExpressionTypeDetector already checks if there are function calls that return
        // no value within the expression(s)

        // now check if the expression return types on LHS and RHS match
        if (lhsType.second != rhsType.second) {
            fmterror(
                "The expression return types on LHS and RHS do not match. " +
                        "Cannot compare ${lhsType.second.asString} on LHS with ${rhsType.second.asString} on RHS.",
                lineNum, Severity.ERROR
            )
            return false
        }

        // check if relational operators can be used with the given types
        if (!lhsType.second.canBeUsedWithRelOp) {
            fmterror(
                "The operator '${op}' cannot be used for the type returned by these expressions " +
                        "(cannot compare two ${lhsType.second.asString} values).",
                lineNum,
                Severity.ERROR
            )
            return false
        }

        // if all checks passed, return true
        return true
    }

    override fun visitBooleanExprCompOp(ctx: SlangGrammarParser.BooleanExprCompOpContext?): Boolean {
        val lhs = ctx!!.expr(0)
        val rhs = ctx.expr(1)

        val typeDetector = ExpressionTypeDetector(symbolTable)
        val lhsType = typeDetector.getType(lhs)
        val rhsType = typeDetector.getType(rhs)

        // utility part to retrieve the line number and compOp representation as string
        // for showing errors if required
        val lineNum: Int
        val op: String
        val theCompOp = ctx.compOp()

        if (theCompOp.COMP() != null) {
            val gtOp = theCompOp.COMP()!!.symbol!!
            lineNum = gtOp.line
            op = gtOp.text
        } else if (theCompOp.COMPNOTEQ() != null) {
            val gtEqOp = theCompOp.COMPNOTEQ()!!.symbol!!
            lineNum = gtEqOp.line
            op = gtEqOp.text
        } else {
            err("[Error] Unknown equality test operator.")
        }

        // check if the LHS and RHS individually have homogenous types within
        if (!lhsType.first || !rhsType.first) {
            fmterror(
                "The expression on ${if (!lhsType.first) "LHS" else "RHS"} " +
                        "${if (!rhsType.first) "and RHS" else ""} " +
                        "has mixed types in it and cannot be evaluated.", lineNum, Severity.ERROR
            )
            return false
        }

        // note that the ExpressionTypeDetector already checks if there are function calls that return
        // no value within the expression(s)

        // now check if the expression return types on LHS and RHS match
        if (lhsType.second != rhsType.second) {
            fmterror(
                "The expression return types on LHS and RHS do not match. " +
                        "Cannot compare ${lhsType.second.asString} on LHS with ${rhsType.second.asString} on RHS.",
                lineNum, Severity.ERROR
            )
            return false
        }

        // check if relational operators can be used with the given types
        if (!lhsType.second.canBeUsedWithCompOp) {
            fmterror(
                "The operator '${op}' cannot be used for the type returned by these expressions " +
                        "(cannot compare two ${lhsType.second.asString} values).",
                lineNum,
                Severity.ERROR
            )
            return false
        }

        // if all checks passed, return true
        return true
    }

    override fun visitBooleanExprParen(ctx: SlangGrammarParser.BooleanExprParenContext?): Boolean {
        return checkUnaryOp(ctx!!)
    }

    override fun visitBooleanExprIdentifier(ctx: SlangGrammarParser.BooleanExprIdentifierContext?): Boolean {
        return checkIdentifierTypeInExpr(ctx!!, SymbolType.BOOL)
    }

    override fun visitBooleanTrue(ctx: SlangGrammarParser.BooleanTrueContext?): Boolean {
        return true
    }

    override fun visitBooleanFalse(ctx: SlangGrammarParser.BooleanFalseContext?): Boolean {
        // since we are just checking types, it returns true
        return true
    }

    override fun visitFunctionCallWithArgs(ctx: SlangGrammarParser.FunctionCallWithArgsContext?): Boolean {
        // check if the function call returns a boolean value
        val returnType = FunctionCallExprChecker.getRetTypeOfFunctionCallWithArgs(ctx, symbolTable)
        if (returnType != SymbolType.BOOL) {
            fmterror(
                "Expected return type was ${SymbolType.BOOL.asString} but the function call returns " +
                        "value of type ${returnType.asString}.", ctx!!.IDENTIFIER().symbol.line, Severity.ERROR
            )
            return false
        }

        return true
    }

    override fun visitFunctionCallNoArgs(ctx: SlangGrammarParser.FunctionCallNoArgsContext?): Boolean {
        val returnType = FunctionCallExprChecker.getRetTypeOfFunctionCallNoArgs(ctx, symbolTable)
        if (returnType != SymbolType.BOOL) {
            fmterror(
                "Expected return type was ${SymbolType.BOOL.asString} but the function call returns " +
                        "value of type ${returnType.asString}.", ctx!!.IDENTIFIER().symbol.line, Severity.ERROR
            )
            return false
        }

        return true
    }
}