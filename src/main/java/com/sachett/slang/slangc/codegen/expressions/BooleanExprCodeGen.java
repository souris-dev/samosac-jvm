package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import org.objectweb.asm.Opcodes;

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
    public Void visitBooleanExprRelOp(SlangParser.BooleanExprRelOpContext ctx) {
        return super.visitBooleanExprRelOp(ctx);
    }

    @Override
    public Void visitBooleanFunctionCall(SlangParser.BooleanFunctionCallContext ctx) {
        return super.visitBooleanFunctionCall(ctx);
    }

    @Override
    public Void visitBooleanExprOr(SlangParser.BooleanExprOrContext ctx) {
        // TODO: implement short circuiting
        visit(ctx.booleanExpr(0));
        visit(ctx.booleanExpr(1));
        functionCodeGen.getMv().visitInsn(Opcodes.IOR);
        return null;
    }

    @Override
    public Void visitBooleanExprNot(SlangParser.BooleanExprNotContext ctx) {
        // I couldn't find a JVM "not" instruction.
        // So, here's a little trick: xoring anything with true gives its complement
        visit(ctx.booleanExpr());
        functionCodeGen.getMv().visitLdcInsn(true);
        functionCodeGen.getMv().visitInsn(Opcodes.IXOR);
        return null;
    }

    @Override
    public Void visitBooleanExprCompOp(SlangParser.BooleanExprCompOpContext ctx) {
        return super.visitBooleanExprCompOp(ctx);
    }

    @Override
    public Void visitBooleanExprParen(SlangParser.BooleanExprParenContext ctx) {
        visit(ctx.booleanExpr());
        return null;
    }

    @Override
    public Void visitBooleanExprIdentifier(SlangParser.BooleanExprIdentifierContext ctx) {
        return super.visitBooleanExprIdentifier(ctx);
    }

    @Override
    public Void visitBooleanTrue(SlangParser.BooleanTrueContext ctx) {
        functionCodeGen.getMv().visitLdcInsn(true);
        return null;
    }

    @Override
    public Void visitBooleanFalse(SlangParser.BooleanFalseContext ctx) {
        functionCodeGen.getMv().visitLdcInsn(false);
        return null;
    }

    @Override
    public Void visitBooleanExprXor(SlangParser.BooleanExprXorContext ctx) {
        visit(ctx.booleanExpr(0));
        visit(ctx.booleanExpr(1));
        functionCodeGen.getMv().visitInsn(Opcodes.IXOR); // TODO: TEST IF THIS WORKS!
        return null;
    }

    @Override
    public Void visitBooleanExprAnd(SlangParser.BooleanExprAndContext ctx) {
        // TODO: implement short circuiting
        visit(ctx.booleanExpr(0));
        visit(ctx.booleanExpr(1));
        functionCodeGen.getMv().visitInsn(Opcodes.IAND);
        return null;
    }
}
