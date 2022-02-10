package com.sachett.slang.slangc.codegen;

import com.sachett.slang.slangc.codegen.expressions.IntExprCodeGen;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.symbol.*;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Objects;

public class ClassFileGenerator extends SlangBaseVisitor<Void> {
    private final TraceClassVisitor classWriter;
    private final ClassWriter delegateClassWriter;
    private SlangParser.ProgramContext programContext;
    private String fileName;
    private String className;
    private final FunctionCodeGen mainMethodVisitor;
    private final SymbolTable symbolTable;

    public ClassFileGenerator(
            SlangParser.ProgramContext programContext,
            @NotNull String fileName,
            @NotNull SymbolTable symbolTable
    ) {
        this.programContext = programContext;
        this.fileName = fileName;
        this.symbolTable = symbolTable;

        // ensure that the symbol table's currentScopeIndex is reset
        symbolTable.resetScopeIndex();

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
        this.delegateClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        this.classWriter = new TraceClassVisitor(delegateClassWriter, new PrintWriter(System.out));
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, this.className, null, "java/lang/Object", null);

        // Generate a default main function
        mainMethodVisitor = new FunctionCodeGen(
                classWriter,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null
        );
        mainMethodVisitor.getMv().visitCode();
    }

    public void generateClass() {
        this.visit(this.programContext);
        mainMethodVisitor.getMv().visitInsn(Opcodes.RETURN);
        mainMethodVisitor.getMv().visitMaxs(0, 0); // any arguments work, will be recalculated
        mainMethodVisitor.getMv().visitEnd();
        classWriter.visitEnd();
    }

    public void writeClass() {
        byte[] classBytes = delegateClassWriter.toByteArray();
        try (FileOutputStream stream
                     = FileUtils.openOutputStream(new File("./out/" + this.className + ".class"))) {
            stream.write(classBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ISymbol makeFieldFromSymbol(String idName) {
        ISymbol symbol = symbolTable.lookup(idName);

        if (symbol == null) {
            return null;
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
                    Opcodes.ACC_STATIC + Opcodes.ACC_PUBLIC,
                    symbol.getName(),
                    Type.getType(String.class).getDescriptor(),
                    null,
                    ((StringSymbol) symbol).getValue()
            ).visitEnd();
        }

        return symbol;
    }

    private void initializeField(ISymbol symbol, @Nullable SlangParser.ExprContext initExpr) {
        switch (symbol.getSymbolType()) {
            case INT:
                // Compile-time evaluated expression
                if (symbol.isInitialValueCalculated()) {
                    mainMethodVisitor.getMv().visitLdcInsn(((IntSymbol) symbol).getValue());
                }
                else {
                    // Runtime evaluation
                    IntExprCodeGen intExprCodeGen = new IntExprCodeGen(
                            initExpr,
                            symbolTable,
                            mainMethodVisitor,
                            className,
                            ""
                    );
                    intExprCodeGen.doCodeGen();
                }

                mainMethodVisitor.getMv().visitFieldInsn(
                        Opcodes.PUTSTATIC,
                        className,
                        symbol.getName(),
                        Type.INT_TYPE.getDescriptor()
                );
                break;

            case BOOL:
                if (symbol.isInitialValueCalculated()) {
                    mainMethodVisitor.getMv().visitLdcInsn(((BoolSymbol) symbol).getValue());
                }
                // TODO: BoolExprCodeGen to be implemented
                mainMethodVisitor.getMv().visitFieldInsn(
                        Opcodes.PUTSTATIC,
                        className,
                        symbol.getName(),
                        Type.BOOLEAN_TYPE.getDescriptor()
                );
                break;

            case STRING:
                if (symbol.isInitialValueCalculated()) {
                    mainMethodVisitor.getMv().visitLdcInsn(((StringSymbol) symbol).getValue());
                }
                // TODO: StringExprCodeGen to be implemented
                mainMethodVisitor.getMv().visitFieldInsn(
                        Opcodes.PUTSTATIC,
                        className,
                        symbol.getName(),
                        Type.getType(String.class).getDescriptor()
                );
                break;
        }
    }

    @Override
    public Void visitBlock(SlangParser.BlockContext ctx) {
        // keep track of scopes in the symbol table
        symbolTable.incrementScope();
        super.visitBlock(ctx);
        symbolTable.decrementScope();
        return null;
    }

    @Override
    public Void visitDeclStmt(SlangParser.DeclStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = makeFieldFromSymbol(idName);
        if (symbol != null) {
            initializeField(symbol, null);
        }
        return null;
    }

    @Override
    public Void visitNormalDeclAssignStmt(SlangParser.NormalDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = makeFieldFromSymbol(idName);
        if (symbol != null) {
            initializeField(symbol, ctx.expr());
        }
        return null;
    }

    @Override
    public Void visitTypeInferredDeclAssignStmt(SlangParser.TypeInferredDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = makeFieldFromSymbol(idName);
        if (symbol != null) {
            initializeField(symbol, ctx.expr());
        }
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SlangParser.FunctionCallNoArgsContext ctx) {
        return null;
    }

    @Override
    public Void visitFunctionCallWithArgs(SlangParser.FunctionCallWithArgsContext ctx) {
        return null;
    }

    @Override
    public Void visitImplicitRetTypeFuncDef(SlangParser.ImplicitRetTypeFuncDefContext ctx) {
        return null;
    }

    @Override
    public Void visitExplicitRetTypeFuncDef(SlangParser.ExplicitRetTypeFuncDefContext ctx) {
        return null;
    }
}
