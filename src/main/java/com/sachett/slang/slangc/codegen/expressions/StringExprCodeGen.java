package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;

public class StringExprCodeGen extends SlangBaseVisitor<Void> implements IExprCodeGen {
    private final SlangParser.ExprContext exprContext;
    private final FunctionCodeGen functionCodeGen;
    private SymbolTable symbolTable;
    private String qualifiedClassName;

    public StringExprCodeGen(
            SlangParser.ExprContext exprContext,
            SymbolTable symbolTable,
            FunctionCodeGen functionCodeGen,
            String className,
            String packageName
    ) {
        this.exprContext = exprContext;
        this.functionCodeGen = functionCodeGen;
        this.symbolTable = symbolTable;
        this.qualifiedClassName = packageName.replace(".", "/") + className;
    }

    @Override
    public void doCodeGen() {
        visit(this.exprContext);
    }
}
