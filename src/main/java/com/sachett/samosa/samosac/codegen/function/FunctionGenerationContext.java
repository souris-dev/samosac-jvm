package com.sachett.samosa.samosac.codegen.function;

import com.sachett.samosa.samosac.codegen.ClassFileGenerator;
import com.sachett.samosa.samosac.symbol.FunctionSymbol;
import com.sachett.samosa.samosac.symbol.ISymbol;
import org.apache.bcel.util.ClassPath;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.HashMap;

import com.sachett.samosa.logging.LoggingUtilsKt;
import org.objectweb.asm.util.TraceClassVisitor;

public class FunctionGenerationContext {
    private final MethodVisitor methodVisitor;
    private final LocalVariablesSorter localVariablesSorter;
    private final AnalyzerAdapter analyzerAdapter;
    private final HashMap<String, Integer> localVariableIndex = new HashMap<>();
    private ClassFileGenerator parentClassGenerator = null;

    /**
     * Indicates if the function needs a RETURN instruction (with no expression)
     * at the end of the function visit.
     */
    private boolean needsNoExprReturn = false;

    public FunctionGenerationContext(
            ClassWriter classWriter,
            int access, String name, String descriptor,
            String signature, String[] exceptions
    ) {
        this.methodVisitor = classWriter.visitMethod(access, name, descriptor, signature, exceptions);
        analyzerAdapter = new AnalyzerAdapter(
                FunctionGenerationContext.class.getName(),
                access, name, descriptor, this.methodVisitor
        );
        localVariablesSorter = new LocalVariablesSorter(access, descriptor, analyzerAdapter);
    }

    public FunctionGenerationContext(
            TraceClassVisitor classWriter,
            int access, String name, String descriptor,
            String signature, String[] exceptions
    ) {
        this.methodVisitor = classWriter.visitMethod(access, name, descriptor, signature, exceptions);
        analyzerAdapter = new AnalyzerAdapter(
                FunctionGenerationContext.class.getName(),
                access, name, descriptor, this.methodVisitor
        );
        localVariablesSorter = new LocalVariablesSorter(access, descriptor, this.methodVisitor);
    }

    public MethodVisitor getMethodVisitor() {
        return methodVisitor;
    }

    public MethodVisitor getMv() {
        return analyzerAdapter;
    }

    public AnalyzerAdapter getAnalyzerAdapter() {
        return analyzerAdapter;
    }

    public void setParentClassGenerator(ClassFileGenerator classFileGenerator) {
        this.parentClassGenerator = classFileGenerator;
    }

    public ClassFileGenerator getParentClassGenerator() {
        return this.parentClassGenerator;
    }

    public static class FrameStackMap {
        public int numLocals;
        public Object[] locals;
        public int numStack;
        public Object[] stack;

        public FrameStackMap(int numLocals, Object[] locals, int numStack, Object[] stack) {
            this.numLocals = numLocals;
            this.locals = locals;
            this.numStack = numStack;
            this.stack = stack;
        }
    }

    /**
     * Should be used before a visitJump or visitLabel call to retrieve status of current stack and locals.
     * @return  Locals and stack of current frame
     */
    public FrameStackMap getCurrentFrameStackInfo() {
        var localsSize = analyzerAdapter.locals == null ? 0 : analyzerAdapter.locals.size();
        var locals = analyzerAdapter.locals == null ? null : analyzerAdapter.locals.toArray();
        var stackSize = analyzerAdapter.stack == null ? 0 : analyzerAdapter.stack.size();
        var stack = analyzerAdapter.stack == null ? null : analyzerAdapter.stack.toArray();

        return new FrameStackMap(localsSize, locals, stackSize, stack);
    }

    public LocalVariablesSorter getLocalVariablesSorter() {
        return localVariablesSorter;
    }

    public void newLocal(String name, Type type) {
        localVariableIndex.put(name, localVariablesSorter.newLocal(type));
    }

    public void registerLocal(String name, int index) {
        localVariableIndex.put(name, index);
    }

    static public String generateDescriptor(FunctionSymbol functionSymbol) {
        StringBuilder descriptorString = new StringBuilder("(");

        for (ISymbol symbol : functionSymbol.getParamList()) {
            switch (symbol.getSymbolType()) {
                case INT:
                    descriptorString.append("I");
                    break;
                case STRING:
                    descriptorString.append("Ljava/lang/String;");
                    break;
                case BOOL:
                    descriptorString.append("Z");
                    break;
            }
        }
        descriptorString.append(")");
        switch (functionSymbol.getReturnType()) {
            case INT:
                descriptorString.append("I");
                break;
            case STRING:
                descriptorString.append("Ljava/lang/String;");
                break;
            case BOOL:
                descriptorString.append("Z");
                break;
            case VOID:
                descriptorString.append("V");
                break;
        }

        return descriptorString.toString();
    }

    public Integer getLocalVarIndex(String name) {
        if (localVariableIndex.containsKey(name)) {
            return localVariableIndex.get(name);
        }
        else {
            LoggingUtilsKt.err("Internal error: Invalid local variable demanded.");
            throw new RuntimeException();
        }
    }

    public void setNeedsNoExprReturn(boolean needsNoExprReturn) {
        this.needsNoExprReturn = needsNoExprReturn;
    }

    public boolean getNeedsNoExprReturn() {
        return this.needsNoExprReturn;
    }
}
