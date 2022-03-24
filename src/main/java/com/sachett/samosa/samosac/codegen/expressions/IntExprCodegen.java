package com.sachett.samosa.samosac.codegen.expressions;

import com.sachett.samosa.parser.SamosaBaseVisitor;
import com.sachett.samosa.parser.SamosaParser;
import com.sachett.samosa.samosac.codegen.function.FunctionCallCodegen;
import com.sachett.samosa.samosac.codegen.function.FunctionGenerationContext;
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class IntExprCodegen extends SamosaBaseVisitor<Void> implements IExprCodegen {
    private SamosaParser.ExprContext exprContext;
    private final FunctionGenerationContext functionGenerationContext;
    private final SymbolTable symbolTable;
    private final String qualifiedClassName;
    private final String className;
    private final String packageName;

    public IntExprCodegen(
            SamosaParser.ExprContext exprContext,
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

    public void setExprContext(SamosaParser.ExprContext exprContext) {
        this.exprContext = exprContext;
    }

    @Override
    public Void visitExprDecint(SamosaParser.ExprDecintContext ctx) {
        int number = Integer.parseInt(ctx.DECINT().getText());
        functionGenerationContext.getMv().visitLdcInsn(number);
        return null;
    }

    @Override
    public Void visitExprParen(SamosaParser.ExprParenContext ctx) {
        visit(ctx.expr());
        return null;
    }

    @Override
    public Void visitExprPlus(SamosaParser.ExprPlusContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionGenerationContext.getMv().visitInsn(Opcodes.IADD);
        return null;
    }

    @Override
    public Void visitExprMinus(SamosaParser.ExprMinusContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionGenerationContext.getMv().visitInsn(Opcodes.ISUB);
        return null;
    }

    @Override
    public Void visitExprMultiply(SamosaParser.ExprMultiplyContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionGenerationContext.getMv().visitInsn(Opcodes.IMUL);
        return null;
    }

    @Override
    public Void visitExprDivide(SamosaParser.ExprDivideContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionGenerationContext.getMv().visitInsn(Opcodes.IDIV);
        return null;
    }

    @Override
    public Void visitExprModulo(SamosaParser.ExprModuloContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionGenerationContext.getMv().visitInsn(Opcodes.IREM);
        return null;
    }

    @Override
    public Void visitUnaryMinus(SamosaParser.UnaryMinusContext ctx) {
        visit(ctx.expr());
        functionGenerationContext.getMv().visitInsn(Opcodes.INEG);
        return null;
    }

    @Override
    public Void visitExprIdentifier(SamosaParser.ExprIdentifierContext ctx) {
        String idName = ctx.IDENTIFIER().getText();
        doIdentifierCodegen(idName, symbolTable, Type.INT_TYPE, functionGenerationContext, qualifiedClassName, Opcodes.ILOAD);
        return null;
    }

    @Override
    public Void visitFunctionCallWithArgs(SamosaParser.FunctionCallWithArgsContext ctx) {
        FunctionCallCodegen functionCallCodegen = new FunctionCallCodegen(
                symbolTable, className, functionGenerationContext, className, packageName
        );
        functionCallCodegen.doWithArgFunctionCallCodegen(ctx, false); // do not discard result
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SamosaParser.FunctionCallNoArgsContext ctx) {
        FunctionCallCodegen functionCallCodegen = new FunctionCallCodegen(
                symbolTable, className, functionGenerationContext, className, packageName
        );
        functionCallCodegen.doNoArgFunctionCallCodegen(ctx, false); // do not discard result
        return null;
    }
}
