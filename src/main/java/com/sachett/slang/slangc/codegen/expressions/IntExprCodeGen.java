package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.symbol.ISymbol;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class IntExprCodeGen extends SlangBaseVisitor<Void> {
    private final SlangParser.ExprContext exprContext;
    private final FunctionCodeGen functionCodeGen;
    private SymbolTable symbolTable;
    private String qualifiedClassName;

    public IntExprCodeGen(
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

    public void doCodeGen() {
        visit(exprContext);
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
        Pair<ISymbol, Integer> lookupInfo = symbolTable.lookupWithNearestScopeValue(idName);
        if (lookupInfo.getFirst() == null) {
            // lookup failed
            return null;
        }

        if (lookupInfo.getSecond() == 0) {
            // we're talking about a global variable
            // (a static field of the class during generation)
            functionCodeGen.getMv().visitFieldInsn(
                    Opcodes.GETSTATIC, qualifiedClassName, idName, Type.INT_TYPE.getDescriptor()
            );
        }
        else {
            Integer localVarIndex = functionCodeGen.getLocalVarIndex(idName);
            functionCodeGen.getMv().visitVarInsn(Opcodes.ILOAD, localVarIndex);
        }
        return null;
    }

    @Override
    public Void visitFunctionCallWithArgs(SlangParser.FunctionCallWithArgsContext ctx) {
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SlangParser.FunctionCallNoArgsContext ctx) {
        return null;
    }
}
