package com.sachett.slang.slangc.staticchecker

import com.sachett.slang.parser.SlangGrammarBaseVisitor
import com.sachett.slang.parser.SlangGrammarParser
import com.sachett.slang.slangc.symbol.BoolSymbol
import com.sachett.slang.slangc.symbol.ISymbol
import com.sachett.slang.slangc.symbol.IntSymbol
import com.sachett.slang.slangc.symbol.StringSymbol
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable

class StaticTypesChecker(private val symbolTable: SymbolTable) : SlangGrammarBaseVisitor<Void>() {

    /* --------------------- Utility functions ----------------------- */

    private fun processArgList(argParamCtx: SlangGrammarParser.ArgParamContext): ISymbol {
        val idName = argParamCtx.IDENTIFIER().text
        val definedOnLineNum = argParamCtx.IDENTIFIER().symbol.line
        val typeNameCtx = argParamCtx.typeName()

        /**
         * We increment the scope once here and decrement it again after inserting the
         * symbols present in the parameter list.
         * Not creating a new scope on the next scope increment (when it enters the block)
         * preserves the parameters that have been inserted already into the function's scope.
         * The decrement is necessary because when the compiler enters the block, it will
         * increment its scope again. We want the parameters to stay.
         */

        symbolTable.incrementScope()

        var symbol: ISymbol? = null

        if (typeNameCtx.INTTYPE() != null) {
            val intSymbol = IntSymbol(idName, definedOnLineNum)
            symbolTable.insert(idName, intSymbol)
            symbol = intSymbol
        } else if (typeNameCtx.STRINGTYPE() != null) {
            val stringSymbol = StringSymbol(idName, definedOnLineNum)
            symbolTable.insert(idName, stringSymbol)
            symbol = stringSymbol
        } else if (typeNameCtx.BOOLTYPE() != null) {
            val boolSymbol = BoolSymbol(idName, definedOnLineNum)
            symbolTable.insert(idName, boolSymbol)
            symbol = boolSymbol
        } else if (typeNameCtx.VOIDTYPE() != null) {
            error("[Error, Line ${definedOnLineNum}] Void type variables are not supported. " +
                    "What did you expect though...?")
        }

        symbolTable.decrementScope(false)
        return symbol!!
    }

    private fun parseAndAddFunctionParamsExplicitDef(
        ctx: SlangGrammarParser.ExplicitRetTypeFuncDefContext
    ): ArrayList<ISymbol> {
        val paramList: ArrayList<ISymbol> = arrayListOf()

        ctx.funcArgList().args.forEach {
            paramList.add(processArgList(it))
        }
        
        return paramList
    }

    private fun parseAndAddFunctionParamsImplicitDef(
        ctx: SlangGrammarParser.ExplicitRetTypeFuncDefContext
    ): ArrayList<ISymbol> {
        val paramList: ArrayList<ISymbol> = arrayListOf()

        ctx.funcArgList().args.forEach {
            paramList.add(processArgList(it))
        }

        return paramList
    }

    /* -----------------  Visitor overrides -------------------- */

    override fun visitProgram(ctx: SlangGrammarParser.ProgramContext?): Void {
        println("Visiting program...")
        return super.visitProgram(ctx)
    }

    override fun visitBlock(ctx: SlangGrammarParser.BlockContext?): Void {
        println("Visiting block...")
        symbolTable.incrementScope()
        val blockVisit = super.visitBlock(ctx)
        symbolTable.decrementScope()
        return blockVisit
    }

    override fun visitDeclStmt(ctx: SlangGrammarParser.DeclStmtContext?): Void {
        return super.visitDeclStmt(ctx)
    }

    override fun visitNormalDeclAssignStmt(ctx: SlangGrammarParser.NormalDeclAssignStmtContext?): Void {
        return super.visitNormalDeclAssignStmt(ctx)
    }

    override fun visitBooleanDeclAssignStmt(ctx: SlangGrammarParser.BooleanDeclAssignStmtContext?): Void {
        return super.visitBooleanDeclAssignStmt(ctx)
    }

    override fun visitExprAssign(ctx: SlangGrammarParser.ExprAssignContext?): Void {
        return super.visitExprAssign(ctx)
    }

    override fun visitExprIdentifier(ctx: SlangGrammarParser.ExprIdentifierContext?): Void {
        return super.visitExprIdentifier(ctx)
    }

    override fun visitFunctionCallWithArgs(ctx: SlangGrammarParser.FunctionCallWithArgsContext?): Void {
        return super.visitFunctionCallWithArgs(ctx)
    }

    override fun visitFunctionCallNoArgs(ctx: SlangGrammarParser.FunctionCallNoArgsContext?): Void {
        return super.visitFunctionCallNoArgs(ctx)
    }

    override fun visitBooleanExprAssign(ctx: SlangGrammarParser.BooleanExprAssignContext?): Void {
        return super.visitBooleanExprAssign(ctx)
    }

    override fun visitImplicitRetTypeFuncDef(ctx: SlangGrammarParser.ImplicitRetTypeFuncDefContext?): Void {
        return super.visitImplicitRetTypeFuncDef(ctx)
    }

    override fun visitExplicitRetTypeFuncDef(ctx: SlangGrammarParser.ExplicitRetTypeFuncDefContext?): Void {
        return super.visitExplicitRetTypeFuncDef(ctx)
    }

    override fun visitIfStmt(ctx: SlangGrammarParser.IfStmtContext?): Void {
        return super.visitIfStmt(ctx)
    }

    override fun visitWhileStmt(ctx: SlangGrammarParser.WhileStmtContext?): Void {
        return super.visitWhileStmt(ctx)
    }
}