package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class IntExprCodeGen extends SlangBaseVisitor<Void> {
    private final SlangParser.ExprContext exprContext;
    private final MethodVisitor methodVisitor;
    private SymbolTable symbolTable;

    public IntExprCodeGen(
            SlangParser.ExprContext exprContext,
            SymbolTable symbolTable,
            MethodVisitor methodVisitor
    ) {
        this.exprContext = exprContext;
        this.methodVisitor = methodVisitor;
        this.symbolTable = symbolTable;
    }

    public void doCodeGen() {
        visit(exprContext);
    }

    @Override
    public Void visitExprDecint(SlangParser.ExprDecintContext ctx) {
        int number = Integer.parseInt(ctx.DECINT().getText());
        methodVisitor.visitIntInsn(Opcodes.ILOAD, number);
        return super.visitExprDecint(ctx);
    }

    @Override
    public Void visitExprParen(SlangParser.ExprParenContext ctx) {
        visit(ctx.expr());
        return super.visitExprParen(ctx);
    }

    @Override
    public Void visitExprPlus(SlangParser.ExprPlusContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        methodVisitor.visitInsn(Opcodes.IADD);
        return super.visitExprPlus(ctx);
    }

    @Override
    public Void visitExprMinus(SlangParser.ExprMinusContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        methodVisitor.visitInsn(Opcodes.ISUB);
        return super.visitExprMinus(ctx);
    }

    @Override
    public Void visitExprMultiply(SlangParser.ExprMultiplyContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        methodVisitor.visitInsn(Opcodes.IMUL);
        return super.visitExprMultiply(ctx);
    }

    @Override
    public Void visitExprDivide(SlangParser.ExprDivideContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        methodVisitor.visitInsn(Opcodes.IDIV);
        return super.visitExprDivide(ctx);
    }

    @Override
    public Void visitExprModulo(SlangParser.ExprModuloContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        methodVisitor.visitInsn(Opcodes.IREM);
        return super.visitExprModulo(ctx);
    }

    @Override
    public Void visitUnaryMinus(SlangParser.UnaryMinusContext ctx) {
        visit(ctx.expr());
        methodVisitor.visitInsn(Opcodes.INEG);
        return super.visitUnaryMinus(ctx);
    }

    @Override
    public Void visitExprIdentifier(SlangParser.ExprIdentifierContext ctx) {
        String idName = ctx.IDENTIFIER().getText();
        return super.visitExprIdentifier(ctx);
    }

    @Override
    public Void visitFunctionCallWithArgs(SlangParser.FunctionCallWithArgsContext ctx) {
        return super.visitFunctionCallWithArgs(ctx);
    }
}
