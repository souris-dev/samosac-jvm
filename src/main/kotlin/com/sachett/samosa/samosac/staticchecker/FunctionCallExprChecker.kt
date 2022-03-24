package com.sachett.samosa.samosac.staticchecker

import com.sachett.samosa.builtins.Builtins
import com.sachett.samosa.logging.Severity
import com.sachett.samosa.logging.err
import com.sachett.samosa.logging.fmterror
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.symbol.FunctionSymbol
import com.sachett.samosa.samosac.symbol.SymbolType
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable

/**
 * Provides utility methods for type checking function call expressions.
 * This class is not supposed to be instantiated; rather, the companion object methods should be used.
 */
class FunctionCallExprChecker {
    companion object {
        /**
         * Type checks the passed arguments in a function call, checks the function's existence
         * and returns the SymbolType that corresponds to the return value of the function call.
         * @param   ctx
         * @param   symbolTable   SymbolTable to be used
         * @return  SymbolType that corresponds to the return value of the function call
         */
        fun getRetTypeOfFunctionCallWithArgs(
            ctx: SamosaParser.FunctionCallWithArgsContext?,
            symbolTable: SymbolTable
        ): SymbolType {
            val funcIdName = ctx?.IDENTIFIER()?.text
            val lineNumber = ctx?.IDENTIFIER()?.symbol?.line
            var symbol = symbolTable.lookup(funcIdName!!)
            var isBuiltin: Boolean = false

            var builtinFuncSymbolOverloads: List<FunctionSymbol>? = null

            // Check if it is a built-in
            if (symbol == null) {
                symbol = symbolTable.lookupBuiltinFunction(funcIdName, null)?.first

                if (symbol == null) {
                    err("[Error, Line ${lineNumber}] Unknown identifier ${funcIdName}.")
                }
                else {
                    isBuiltin = true
                    builtinFuncSymbolOverloads = symbolTable
                        .lookupBuiltinFunctionAllOverloads(funcIdName)
                        ?.map { it.first }
                }
            }

            if (!isBuiltin) {
                if (!symbol.isSymbolType(SymbolType.FUNCTION)) {
                    err("[Error, Line ${lineNumber}] Cannot call $funcIdName as a function. " +
                            "It is not a function but rather an identifier of " +
                            "type ${symbol.symbolType.asString}.")
                }

                val functionSymbol = symbol as FunctionSymbol
                val calledParamsOk = isCalledParamListHavingValidTypes(ctx, functionSymbol, symbolTable)

                if (!calledParamsOk) {
                    // passed arguments did not pass type check

                    err("[Error, Line ${lineNumber}] Bad arguments passed to function in call to ${funcIdName}. " +
                            "(There is probably additional detail above this message.)")
                }
                // if all okay, return the function symbol's return type
                return functionSymbol.returnType
            }
            else {
                // generate the descriptor from the ctx
                val expectedDescriptor = Builtins.Functions.Utils.ctxToDescriptor(ctx, symbolTable)
                val functionSymbol = symbolTable.lookupBuiltinFunctionMatchingOverload(funcIdName, expectedDescriptor)

                if (functionSymbol == null) {
                    var expectedArgs = ""
                    var count = 1
                    for (overload in builtinFuncSymbolOverloads!!) {
                        expectedArgs += "$count. ("
                        overload.paramList.forEach {
                            expectedArgs += it.symbolType.asString + ", "
                        }
                        expectedArgs = expectedArgs.trimEnd(' ', ',')
                        expectedArgs += ")\n"
                        count++
                    }

                    err("[Error, Line ${lineNumber}] Bad arguments passed to function in call to ${funcIdName}. " +
                            "Use one of the following overloads: \n$expectedArgs")
                }

                return functionSymbol.first.returnType // as same return type for all overloads
            }
        }

        /**
         * Similar to getRetTypeOfFunctionCallWithArgs() but for function calls with no arguments.
         * Checks for the function's definition and returns the SymbolType that corresponds
         * to the return value of the function call.
         * @param   ctx
         * @param   symbolTable
         * @return  <code>SymbolType</code> that corresponds to the return value of the function call.
         */
        fun getRetTypeOfFunctionCallNoArgs(
            ctx: SamosaParser.FunctionCallNoArgsContext?,
            symbolTable: SymbolTable
        ): SymbolType {
            val funcIdName = ctx?.IDENTIFIER()?.text
            val lineNumber = ctx?.IDENTIFIER()?.symbol?.line
            var symbol = symbolTable.lookup(funcIdName!!)
            var isBuiltin: Boolean = false

            // Check if it is a built-in
            if (symbol == null) {
                symbol = symbolTable.lookupBuiltinFunction(funcIdName, null)?.first

                if (symbol == null) {
                    err("[Error, Line ${lineNumber}] Unknown identifier ${funcIdName}.")
                }
                else {
                    isBuiltin = true
                }
            }

            if (!isBuiltin) {
                if (!symbol.isSymbolType(SymbolType.FUNCTION)) {
                    err("[Error, Line ${lineNumber}] Cannot call $funcIdName as a function. " +
                            "It is not a function but rather an identifier of " +
                            "type ${symbol.symbolType.asString}.")
                }

                val functionSymbol = symbol as FunctionSymbol
                return functionSymbol.returnType
            }
            else {
                val expectedDescriptor = "()"
                val functionSymbol = symbolTable.lookupBuiltinFunctionMatchingOverload(
                    funcIdName, expectedDescriptor
                ) ?: err("[Error, Line ${lineNumber}] Bad arguments passed to function in call to ${funcIdName}. " +
                            "Expected no arguments.")

                return functionSymbol.first.returnType
            }
        }

        /**
         * Type checks the parameters passed to the function in the function call.
         * @param ctx
         * @param functionSymbol    The resolved <code>FunctionSymbol</code> to check against.
         * @param symbolTable       The <code>SymbolTable</code> to be used.
         * @return <code>true</code> if types are valid else <code>false</code>.
         */
        private fun isCalledParamListHavingValidTypes(
            ctx: SamosaParser.FunctionCallWithArgsContext?,
            functionSymbol: FunctionSymbol,
            symbolTable: SymbolTable
        ): Boolean {
            // The call argument list can have two parts: the normal call params and boolean expr call params
            val argListUnprocessed = ctx?.callArgList()?.callParams
            val booleanArgListUnprocessed = ctx?.callArgList()?.booleanCallParams
            val funcName = ctx?.IDENTIFIER()?.text
            val lineNumber = ctx?.IDENTIFIER()?.symbol?.line

            val funcExpectedParamList = functionSymbol.paramList

            val totalArgsPassed = argListUnprocessed?.size!! + booleanArgListUnprocessed?.size!!

            if (funcExpectedParamList.size != totalArgsPassed) {
                err("[Error, Line ${lineNumber}] Expected ${funcExpectedParamList.size} arguments for call " +
                        "to $funcName but $totalArgsPassed arguments were provided.")
            }

            var allParamTypesOk = true

            // these two help us keep track of current array indices
            // of the two parts of the argument list
            var currentPassedArg = 0
            var currentBooleanPassedArg = 0

            repeat(totalArgsPassed) {
                val expressionChecker: ExpressionChecker
                val currentExpectedParam = funcExpectedParamList[it]
                val expectedType: SymbolType = when (currentExpectedParam.symbolType) {
                    SymbolType.INT -> {
                        expressionChecker = IntExpressionChecker(symbolTable)
                        SymbolType.INT
                    }
                    SymbolType.STRING -> {
                        expressionChecker = StringExpressionChecker(symbolTable)
                        SymbolType.STRING
                    }
                    SymbolType.BOOL -> {
                        expressionChecker = BoolExpressionChecker(symbolTable)
                        SymbolType.BOOL
                    }
                    else -> {
                        err("[Error, Line ${functionSymbol.firstAppearedLine}] An argument of type " +
                                "${currentExpectedParam.symbolType.asString} cannot be passed " +
                                "to a function as of now.")
                    }
                }

                // default value for this is false:
                val paramTypeOk: Boolean

                if (currentExpectedParam.isSymbolType(SymbolType.BOOL)) {
                    /**
                     * BooleanExpressionChecker has a different overload of checkExpr()
                     * that takes a BooleanExprContext.
                     */
                    val booleanExpressionChecker = expressionChecker as BoolExpressionChecker
                    paramTypeOk = booleanExpressionChecker.checkExpr(booleanArgListUnprocessed[currentBooleanPassedArg])
                    currentBooleanPassedArg++
                }
                else {
                    // For non-boolean arguments
                    paramTypeOk = expressionChecker.checkExpr(argListUnprocessed[currentPassedArg])
                    currentPassedArg++
                }

                allParamTypesOk = allParamTypesOk && paramTypeOk

                if (!paramTypeOk) {
                    fmterror("Expected type of argument in call to $funcName was ${expectedType.asString} but received expression of different type.", lineNumber!!, Severity.ERROR)
                }
            }

            return allParamTypesOk
        }
    }
}