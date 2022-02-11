package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;

public class BooleanExprCodeGen extends SlangBaseVisitor<Void> implements IExprCodeGen {
    private final SlangParser.BooleanExprContext exprContext;
    private final FunctionCodeGen functionCodeGen;
    private SymbolTable symbolTable;
    private String qualifiedClassName;

    public BooleanExprCodeGen(
            SlangParser.BooleanExprContext exprContext,
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

    @Override
    public Void visitExprString(SlangParser.ExprStringContext ctx) {
        return super.visitExprString(ctx);
    }
}
