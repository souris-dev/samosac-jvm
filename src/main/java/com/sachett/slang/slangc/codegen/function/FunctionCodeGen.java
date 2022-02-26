package com.sachett.slang.slangc.codegen.function;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.Arrays;
import java.util.HashMap;

import com.sachett.slang.logging.LoggingUtilsKt;
import org.objectweb.asm.util.TraceClassVisitor;

public class FunctionCodeGen {
    private final MethodVisitor methodVisitor;
    private final LocalVariablesSorter localVariablesSorter;
    private final AnalyzerAdapter analyzerAdapter;
    HashMap<String, Integer> localVariableIndex = new HashMap<>();

    public FunctionCodeGen(
            ClassWriter classWriter,
            int access, String name, String descriptor,
            String signature, String[] exceptions
    ) {
        this.methodVisitor = classWriter.visitMethod(access, name, descriptor, signature, exceptions);
        analyzerAdapter = new AnalyzerAdapter(
                FunctionCodeGen.class.getName(),
                access, name, descriptor, this.methodVisitor
        );
        localVariablesSorter = new LocalVariablesSorter(access, descriptor, analyzerAdapter);
    }

    public FunctionCodeGen(
            TraceClassVisitor classWriter,
            int access, String name, String descriptor,
            String signature, String[] exceptions
    ) {
        this.methodVisitor = classWriter.visitMethod(access, name, descriptor, signature, exceptions);
        analyzerAdapter = new AnalyzerAdapter(
                FunctionCodeGen.class.getName(),
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

    public Integer getLocalVarIndex(String name) {
        if (localVariableIndex.containsKey(name)) {
            return localVariableIndex.get(name);
        }
        else {
            LoggingUtilsKt.err("Internal error: Invalid local variable demanded.");
            throw new RuntimeException();
        }
    }
}
