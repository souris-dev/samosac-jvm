package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.function.FunctionCallCodegen;
import com.sachett.slang.slangc.codegen.function.FunctionGenerationContext;
import com.sachett.slang.slangc.symbol.SymbolType;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class IntExprCodegen extends SlangBaseVisitor<Void> implements IExprCodegen {
    private SlangParser.ExprContext exprContext;
    private final FunctionGenerationContext functionGenerationContext;
    private final SymbolTable symbolTable;
    private final String qualifiedClassName;
    private final String className;
    private final String packageName;

    public IntExprCodegen(
            SlangParser.ExprContext exprContext,
            SymbolTable symbolTable,
            FunctionGenerationContext functionGenerationContext,
            String className,
            String packageName
    ) {
        this.exprContext = exprContext;
        this.functionGenerationContext = functionGenerationContext;
        this.symbolTable = symbolTable;
        this.qualifiedClassName = packageName.replace(".", "/") + className;
        this.className = className;
        this.packageName = packageName;
    }

    @Override
    public void doCodegen() {
        visit(exprContext);
    }

    public void setExprContext(SlangParser.ExprContext exprContext) {
        this.exprContext = exprContext;
    }

    @Override
    public Void visitExprDecint(SlangParser.ExprDecintContext ctx) {
        int number = Integer.parseInt(ctx.DECINT().getText());
        functionGenerationContext.getMv().visitLdcInsn(number);
        return null;
    }

    @Override
    public Void visitExprParen(SlangParser.ExprParenContext ctx) {
        visit(ctx.expr());
        return null;
    }

    @Override
    public Void visitExprPlus(SlangParser.ExprPlusContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionGenerationContext.getMv().visitInsn(Opcodes.IADD);
        return null;
    }

    @Override
    public Void visitExprMinus(SlangParser.ExprMinusContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionGenerationContext.getMv().visitInsn(Opcodes.ISUB);
        return null;
    }

    @Override
    public Void visitExprMultiply(SlangParser.ExprMultiplyContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionGenerationContext.getMv().visitInsn(Opcodes.IMUL);
        return null;
    }

    @Override
    public Void visitExprDivide(SlangParser.ExprDivideContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionGenerationContext.getMv().visitInsn(Opcodes.IDIV);
        return null;
    }

    @Override
    public Void visitExprModulo(SlangParser.ExprModuloContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionGenerationContext.getMv().visitInsn(Opcodes.IREM);
        return null;
    }

    @Override
    public Void visitUnaryMinus(SlangParser.UnaryMinusContext ctx) {
        visit(ctx.expr());
        functionGenerationContext.getMv().visitInsn(Opcodes.INEG);
        return null;
    }

    @Override
    public Void visitExprIdentifier(SlangParser.ExprIdentifierContext ctx) {
        String idName = ctx.IDENTIFIER().getText();
        doIdentifierCodegen(idName, symbolTable, Type.INT_TYPE, functionGenerationContext, qualifiedClassName, Opcodes.ILOAD);
        return null;
    }

    @Override
    public Void visitFunctionCallWithArgs(SlangParser.FunctionCallWithArgsContext ctx) {
        FunctionCallCodegen functionCallCodegen = new FunctionCallCodegen(
                symbolTable, className, functionGenerationContext, className, packageName
        );
        functionCallCodegen.doWithArgFunctionCallCodegen(ctx, false); // do not discard result
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SlangParser.FunctionCallNoArgsContext ctx) {
        FunctionCallCodegen functionCallCodegen = new FunctionCallCodegen(
                symbolTable, className, functionGenerationContext, className, packageName
        );
        functionCallCodegen.doNoArgFunctionCallCodegen(ctx, false); // do not discard result
        return null;
    }
}
