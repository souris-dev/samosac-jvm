package com.sachett.slang.slangc.generation;

import com.sachett.slang.slangc.symbol.*;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

public class ClassFileGenerator extends SlangBaseVisitor<Void> {
    private final ClassWriter classWriter;
    private SlangParser.ProgramContext programContext;
    private String fileName;
    private String className;
    private final MethodVisitor mainMethodVisitor;
    private final SymbolTable symbolTable;

    public ClassFileGenerator(
            SlangParser.ProgramContext programContext,
            @NotNull String fileName,
            @NotNull SymbolTable symbolTable
    ) {
        this.programContext = programContext;
        this.fileName = fileName;
        this.symbolTable = symbolTable;

        String[] fileNameParts = fileName.split("\\.");
        StringBuilder genClassNameBuilder = new StringBuilder();

        for (String fileNamePart : fileNameParts) {
            // Capitalize the first letter of each part of the filename

            if (Objects.equals(fileNamePart, "")) {
                continue;
            }

            if (fileNamePart.contains("/")) {
                String[] dirParts = fileNamePart.split("[/\\\\]");
                fileNamePart = dirParts.length > 0 ? dirParts[dirParts.length - 1] : fileNamePart;
            }

            String modFileNamePart = fileNamePart.substring(0, 1).toUpperCase() + fileNamePart.substring(1);
            genClassNameBuilder.append(modFileNamePart);
        }

        this.className = genClassNameBuilder.toString();

        // Generate a default class
        this.classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, this.className, null, "java/lang/Object", null);

        // Generate a default main function
        mainMethodVisitor = classWriter.visitMethod(
                Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null
        );
        mainMethodVisitor.visitCode();
    }

    public void generateClass() {
        this.visit(this.programContext);
        mainMethodVisitor.visitInsn(Opcodes.RETURN);
        mainMethodVisitor.visitMaxs(0, 0); // any arguments work, will be recalculated
        mainMethodVisitor.visitEnd();
        classWriter.visitEnd();
    }

    public void writeClass() {
        byte[] classBytes = classWriter.toByteArray();
        try (FileOutputStream stream
                     = FileUtils.openOutputStream(new File("./out/" + this.className + ".class"))) {
            stream.write(classBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeFieldFromSymbol(String idName) {
        ISymbol symbol = symbolTable.lookup(idName);

        if (symbol == null) {
            return;
        }

        SymbolType symbolType = symbol.getSymbolType();
        if (symbolType == SymbolType.INT) {
            classWriter.visitField(
                    Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE,
                    symbol.getName(),
                    Type.INT_TYPE.getDescriptor(),
                    null,
                    ((IntSymbol) symbol).getValue()
            ).visitEnd();
        } else if (symbolType == SymbolType.BOOL) {
            classWriter.visitField(
                    Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE,
                    symbol.getName(),
                    Type.BOOLEAN_TYPE.getDescriptor(),
                    null,
                    ((BoolSymbol) symbol).getValue()
            ).visitEnd();
        } else if (symbolType == SymbolType.STRING) {
            classWriter.visitField(
                    Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE,
                    symbol.getName(),
                    Type.getType(String.class).getDescriptor(),
                    null,
                    ((StringSymbol) symbol).getValue()
            ).visitEnd();
        }
    }

    @Override
    public Void visitDeclStmt(SlangParser.DeclStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        makeFieldFromSymbol(idName);
        return super.visitDeclStmt(ctx);
    }

    @Override
    public Void visitNormalDeclAssignStmt(SlangParser.NormalDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        makeFieldFromSymbol(idName);
        return super.visitNormalDeclAssignStmt(ctx);
    }

    @Override
    public Void visitTypeInferredDeclAssignStmt(SlangParser.TypeInferredDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        makeFieldFromSymbol(idName);
        return super.visitTypeInferredDeclAssignStmt(ctx);
    }
}
