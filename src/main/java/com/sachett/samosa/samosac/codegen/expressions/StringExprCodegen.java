package com.sachett.samosa.samosac.codegen.expressions;

import com.sachett.samosa.logging.LoggingUtilsKt;
import com.sachett.samosa.parser.SamosaBaseVisitor;
import com.sachett.samosa.parser.SamosaParser;
import com.sachett.samosa.samosac.codegen.function.FunctionCallCodegen;
import com.sachett.samosa.samosac.codegen.function.FunctionGenerationContext;
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class StringExprCodegen extends SamosaBaseVisitor<Void> implements IExprCodegen {
    private SamosaParser.ExprContext exprContext;
    private final FunctionGenerationContext functionGenerationContext;
    private final SymbolTable symbolTable;
    private final String qualifiedClassName;
    private final String className;
    private final String packageName;

    public StringExprCodegen(
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
        visit(this.exprContext);
    }

    @Override
    public Void visitExprString(SamosaParser.ExprStringContext ctx) {
        String strText = ctx.getText();
        String str = strText.substring(1, strText.length() - 1);
        functionGenerationContext.getMv().visitLdcInsn(str);
        return null;
    }

    @Override
    public Void visitExprIdentifier(SamosaParser.ExprIdentifierContext ctx) {
        String idName = ctx.IDENTIFIER().getText();
        doIdentifierCodegen(idName, symbolTable, Type.getType(String.class),
                functionGenerationContext, qualifiedClassName, Opcodes.ALOAD);
        return super.visitExprIdentifier(ctx);
    }

    public void setExprContext(SamosaParser.ExprContext exprContext) {
        this.exprContext = exprContext;
    }

    @Override
    public Void visitExprPlus(SamosaParser.ExprPlusContext ctx) {
        // Java equivalent:
        // StringBuilder sb = new StringBuilder("stringLeft");
        // sb.append("stringLeft")

        // Make a StringBuilder object and duplicate it on the stack
        // Note that we need to duplicate it ONE time because we will make three calls on it as follows:
        // 1. <init>: pops off object ref
        // 2. append: pops off object ref, does append, then pushes it back (see descriptor of append)
        // 3. toString: pops off object ref, pushes string representation onto stack
        functionGenerationContext.getMv().visitTypeInsn(Opcodes.NEW, Type.getType(StringBuilder.class).getInternalName());
        functionGenerationContext.getMv().visitInsn(Opcodes.DUP);
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
        functionGenerationContext.getMv().visitMethodInsn(
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
        functionGenerationContext.getMv().visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                Type.getType(StringBuilder.class).getInternalName(),
                "append",
                Type.getMethodDescriptor(stringBuilderAppend),
                false
        );

        // Now get the string representation using toString()
        assert stringBuilderToString != null;
        functionGenerationContext.getMv().visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                Type.getType(StringBuilder.class).getInternalName(),
                "toString",
                Type.getMethodDescriptor(stringBuilderToString),
                false
        );

        return null;
    }

    @Override
    public Void visitExprParen(SamosaParser.ExprParenContext ctx) {
        visit(ctx.expr());
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
