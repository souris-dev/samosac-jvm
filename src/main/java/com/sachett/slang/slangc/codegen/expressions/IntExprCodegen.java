package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.function.FunctionCodegen;
import com.sachett.slang.slangc.symbol.SymbolType;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class IntExprCodegen extends SlangBaseVisitor<Void> implements IExprCodegen {
    private SlangParser.ExprContext exprContext;
    private final FunctionCodegen functionCodeGen;
    private final SymbolTable symbolTable;
    private final String qualifiedClassName;

    public IntExprCodegen(
            SlangParser.ExprContext exprContext,
            SymbolTable symbolTable,
            FunctionCodegen functionCodeGen,
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
        visit(exprContext);
    }

    public void setExprContext(SlangParser.ExprContext exprContext) {
        this.exprContext = exprContext;
    }

    @Override
    public Void visitExprDecint(SlangParser.ExprDecintContext ctx) {
        int number = Integer.parseInt(ctx.DECINT().getText());
        functionCodeGen.getMv().visitLdcInsn(number);
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
        functionCodeGen.getMv().visitInsn(Opcodes.IADD);
        return null;
    }

    @Override
    public Void visitExprMinus(SlangParser.ExprMinusContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionCodeGen.getMv().visitInsn(Opcodes.ISUB);
        return null;
    }

    @Override
    public Void visitExprMultiply(SlangParser.ExprMultiplyContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionCodeGen.getMv().visitInsn(Opcodes.IMUL);
        return null;
    }

    @Override
    public Void visitExprDivide(SlangParser.ExprDivideContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionCodeGen.getMv().visitInsn(Opcodes.IDIV);
        return null;
    }

    @Override
    public Void visitExprModulo(SlangParser.ExprModuloContext ctx) {
        visit(ctx.expr(0)); // visit left operand
        visit(ctx.expr(1)); // visit right operand
        functionCodeGen.getMv().visitInsn(Opcodes.IREM);
        return null;
    }

    @Override
    public Void visitUnaryMinus(SlangParser.UnaryMinusContext ctx) {
        visit(ctx.expr());
        functionCodeGen.getMv().visitInsn(Opcodes.INEG);
        return null;
    }

    @Override
    public Void visitExprIdentifier(SlangParser.ExprIdentifierContext ctx) {
        String idName = ctx.IDENTIFIER().getText();
        doIdentifierCodegen(idName, symbolTable, Type.INT_TYPE, functionCodeGen, qualifiedClassName, Opcodes.ILOAD);
        return null;
    }

    @Override
    public Void visitFunctionCallWithArgs(SlangParser.FunctionCallWithArgsContext ctx) {
        // TODO: This is a DUMMY, to be implemented
        functionCodeGen.getMv().visitLdcInsn(SymbolType.INT.getDefaultValue());
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SlangParser.FunctionCallNoArgsContext ctx) {
        // TODO: This is a DUMMY, to be implemented
        functionCodeGen.getMv().visitLdcInsn(SymbolType.INT.getDefaultValue());
        return null;
    }
}
