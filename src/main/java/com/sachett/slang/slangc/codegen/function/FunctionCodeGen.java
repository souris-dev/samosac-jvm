package com.sachett.slang.slangc.codegen.function;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.HashMap;

import com.sachett.slang.logging.LoggingUtilsKt;
import org.objectweb.asm.util.TraceClassVisitor;

public class FunctionCodeGen {
    private final MethodVisitor methodVisitor;
    private final LocalVariablesSorter localVariablesSorter;
    HashMap<String, Integer> localVariableIndex = new HashMap<>();

    public FunctionCodeGen(
            ClassWriter classWriter,
            int access, String name, String descriptor,
            String signature, String[] exceptions
    ) {
        this.methodVisitor = classWriter.visitMethod(access, name, descriptor, signature, exceptions);
        localVariablesSorter = new LocalVariablesSorter(access, descriptor, this.methodVisitor);
    }

    public FunctionCodeGen(
            TraceClassVisitor classWriter,
            int access, String name, String descriptor,
            String signature, String[] exceptions
    ) {
        this.methodVisitor = classWriter.visitMethod(access, name, descriptor, signature, exceptions);
        localVariablesSorter = new LocalVariablesSorter(access, descriptor, this.methodVisitor);
    }

    public MethodVisitor getMethodVisitor() {
        return methodVisitor;
    }

    public MethodVisitor getMv() {
        return methodVisitor;
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
