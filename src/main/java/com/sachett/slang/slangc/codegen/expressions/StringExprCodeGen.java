package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.logging.LoggingUtilsKt;
import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class StringExprCodeGen extends SlangBaseVisitor<Void> implements IExprCodeGen {
    private final SlangParser.ExprContext exprContext;
    private final FunctionCodeGen functionCodeGen;
    private final SymbolTable symbolTable;
    private final String qualifiedClassName;

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

    @Override
    public Void visitExprString(SlangParser.ExprStringContext ctx) {
        String strText = ctx.getText();
        String str = strText.substring(1, strText.length() - 1);
        functionCodeGen.getMv().visitLdcInsn(str);
        return null;
    }

    @Override
    public Void visitExprIdentifier(SlangParser.ExprIdentifierContext ctx) {
        String idName = ctx.IDENTIFIER().getText();
        doIdentifierCodegen(idName, symbolTable, Type.getType(String.class),
                functionCodeGen, qualifiedClassName, Opcodes.ALOAD);
        return super.visitExprIdentifier(ctx);
    }

    @Override
    public Void visitExprPlus(SlangParser.ExprPlusContext ctx) {
        // Java equivalent:
        // StringBuilder sb = new StringBuilder("stringLeft");
        // sb.append("stringLeft")

        // Make a StringBuilder object and duplicate it on the stack
        // Note that we need to duplicate it ONE time because we will make three calls on it as follows:
        // 1. <init>: pops off object ref
        // 2. append: pops off object ref, does append, then pushes it back (see descriptor of append)
        // 3. toString: pops off object ref, pushes string representation onto stack
        functionCodeGen.getMv().visitTypeInsn(Opcodes.NEW, Type.getType(StringBuilder.class).getInternalName());
        functionCodeGen.getMv().visitInsn(Opcodes.DUP);
        // Process and put the left operand on the stack
        visit(ctx.expr(0));

        // Using Reflection API so that I don't have to write descriptor strings by hand
        Constructor<StringBuilder> stringBuilderConstructor = null;
        Method stringBuilderAppend = null;
        Method stringBuilderToString = null;

        try {
             stringBuilderConstructor = StringBuilder.class.getConstructor(String.class);
             stringBuilderAppend = StringBuilder.class.getMethod("append", String.class);
             stringBuilderToString = StringBuilder.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            LoggingUtilsKt.err(String.valueOf(e));
        }

        // Invoke the constructor of StringBuilder with the left operand
        assert stringBuilderConstructor != null;
        functionCodeGen.getMv().visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                Type.getType(StringBuilder.class).getInternalName(),
                "<init>",
                Type.getConstructorDescriptor(stringBuilderConstructor),
                false
        );

        // Now process the right operand and put it on the stack
        visit(ctx.expr(1));

        // Now invoke append on the StringBuilder object with the right operand
        assert stringBuilderAppend != null;
        functionCodeGen.getMv().visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                Type.getType(StringBuilder.class).getInternalName(),
                "append",
                Type.getMethodDescriptor(stringBuilderAppend),
                false
        );

        // Now get the string representation using toString()
        assert stringBuilderToString != null;
        functionCodeGen.getMv().visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                Type.getType(StringBuilder.class).getInternalName(),
                "toString",
                Type.getMethodDescriptor(stringBuilderToString),
                false
        );

        return null;
    }

    @Override
    public Void visitExprParen(SlangParser.ExprParenContext ctx) {
        visit(ctx.expr());
        return null;
    }

    @Override
    public Void visitFunctionCallWithArgs(SlangParser.FunctionCallWithArgsContext ctx) {
        // TODO: This is a DUMMY, to be implemented
        functionCodeGen.getMv().visitLdcInsn("");
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SlangParser.FunctionCallNoArgsContext ctx) {
        // TODO: This is a DUMMY, to be implemented
        functionCodeGen.getMv().visitLdcInsn("");
        return null;
    }
}
