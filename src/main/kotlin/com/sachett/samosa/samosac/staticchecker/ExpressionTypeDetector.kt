package com.sachett.samosa.samosac.staticchecker

import com.sachett.samosa.logging.Severity
import com.sachett.samosa.logging.err
import com.sachett.samosa.logging.fmterror
import com.sachett.samosa.logging.fmtfatalerr
import com.sachett.samosa.parser.SamosaBaseVisitor
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.symbol.FunctionSymbol
import com.sachett.samosa.samosac.symbol.SymbolType
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable

/**
 * Tries to detect the overall expression type given an expression.
 * The expression type detection is based on the frequencies of the types in it.
 */
class ExpressionTypeDetector(
    private val symbolTable: SymbolTable
) : SamosaBaseVisitor<Unit>() {
    private var symbolTypesInExpr: MutableMap<SymbolType, Int> = mutableMapOf(
        SymbolType.INT to 0,
        SymbolType.STRING to 0,
        SymbolType.BOOL to 0,
        SymbolType.VOID to 0,
        SymbolType.FUNCTION to 0
    )

    private val emptySymbolTypesInExpr: Map<SymbolType, Int> = mapOf(
        SymbolType.INT to 0,
        SymbolType.STRING to 0,
        SymbolType.BOOL to 0,
        SymbolType.VOID to 0,
        SymbolType.FUNCTION to 0
    )

    /**
     * Tries to detect the overall expression type given the ExprContext.
     * The expression type detection is based on the frequencies of the types in it.
     * @param   ctx The ExprContext to work on.
     * @return  <code>Pair<Boolean, SymbolType></code>. The first element is <code>true</code> if
     *          all the terminals are of the same type in the expression and <code>false</code> if not.
     *          The second element is the <code>SymbolType</code> that appears the most times in the expression.
     */
    fun getType(ctx: SamosaParser.ExprContext): Pair<Boolean, SymbolType> {
        println("----- Finding expr type for expr -----") // DEBUG

        // clear the table
        symbolTypesInExpr = emptySymbolTypesInExpr.toMutableMap()
        visit(ctx)

        var maxFreq = 0
        var nTerms = 0

        // if there are no terms in expression then it is a void expression (which is invalid)
        var maxFreqSymbolType: SymbolType = SymbolType.VOID

        symbolTypesInExpr.forEach {
            if (it.value > maxFreq) {
                maxFreq = it.value
                maxFreqSymbolType = it.key
            }
            nTerms += it.value
        }

        return Pair(maxFreq == nTerms, maxFreqSymbolType)
    }

    /* -----------------  Visitor methods -------------------- */

    // check the terminals and retrieve their types

    override fun visitExprDecint(ctx: SamosaParser.ExprDecintContext?) {
        symbolTypesInExpr[SymbolType.INT] = symbolTypesInExpr.getOrDefault(SymbolType.INT, 0) + 1
        return super.visitExprDecint(ctx)
    }

    override fun visitExprString(ctx: SamosaParser.ExprStringContext?) {
        symbolTypesInExpr[SymbolType.STRING] = symbolTypesInExpr.getOrDefault(SymbolType.STRING, 0) + 1
        return super.visitExprString(ctx)
    }

    override fun visitBooleanTrue(ctx: SamosaParser.BooleanTrueContext?) {
        symbolTypesInExpr[SymbolType.BOOL] = symbolTypesInExpr.getOrDefault(SymbolType.BOOL, 0) + 1
        return super.visitBooleanTrue(ctx)
    }

    override fun visitBooleanFalse(ctx: SamosaParser.BooleanFalseContext?) {
        symbolTypesInExpr[SymbolType.BOOL] = symbolTypesInExpr.getOrDefault(SymbolType.BOOL, 0) + 1
        return super.visitBooleanFalse(ctx)
    }

    /**
     * Retrieves the type of identifier.
     */
    override fun visitExprIdentifier(ctx: SamosaParser.ExprIdentifierContext?) {
        val idName = ctx?.IDENTIFIER()?.text
        val lineNumber = ctx?.IDENTIFIER()?.symbol?.line

        val symbol = symbolTable.lookup(idName!!) ?: fmtfatalerr("Unknown identifier ${idName}.", lineNumber!!)

        symbolTypesInExpr[symbol.symbolType] = symbolTypesInExpr.getOrDefault(symbol.symbolType, 0) + 1
        return super.visitExprIdentifier(ctx)
    }

    // The next two functions check the return types of any function calls in the expression

    override fun visitFunctionCallWithArgs(ctx: SamosaParser.FunctionCallWithArgsContext?) {
        val retType: SymbolType = FunctionCallExprChecker.getRetTypeOfFunctionCallWithArgs(ctx, symbolTable)

        val lineNum = ctx!!.IDENTIFIER().symbol.line
        if (retType !in FunctionSymbol.allowedReturnTypes.minus(SymbolType.VOID)) {
            fmtfatalerr(
                "Illegal return type of function call in expression, in call to ${ctx.IDENTIFIER().text}. " +
                        if (retType == SymbolType.VOID) "The function call returns no value." else "",
                lineNum,
            )
        }

        symbolTypesInExpr[retType] = symbolTypesInExpr.getOrDefault(retType, 0) + 1

        /* Do not go into the function call expression here (hence super's method isn't called) */
    }

    override fun visitFunctionCallNoArgs(ctx: SamosaParser.FunctionCallNoArgsContext?) {
        val retType: SymbolType = FunctionCallExprChecker.getRetTypeOfFunctionCallNoArgs(ctx, symbolTable)

        val lineNum = ctx!!.IDENTIFIER().symbol.line
        if (retType !in FunctionSymbol.allowedReturnTypes.minus(SymbolType.VOID)) {
            fmtfatalerr(
                "Illegal return type of function call in expression, in call to ${ctx.IDENTIFIER().text}. " +
                        if (retType == SymbolType.VOID) "The function call returns no value." else "",
                lineNum,
            )
        }

        symbolTypesInExpr[retType] = symbolTypesInExpr.getOrDefault(retType, 0) + 1
        /* Do not go into the function call expression here (hence super's method isn't called) */
    }
}