package com.sachett.slang.slangc.codegen;

import com.sachett.slang.slangc.codegen.expressions.BooleanExprCodeGen;
import com.sachett.slang.slangc.codegen.expressions.IntExprCodeGen;
import com.sachett.slang.slangc.codegen.expressions.StringExprCodeGen;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.staticchecker.ExpressionTypeDetector;
import com.sachett.slang.slangc.symbol.*;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import kotlin.Pair;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Types;
import java.util.HashMap;
import java.util.Objects;

import static com.sachett.slang.logging.LoggingUtilsKt.err;

public class ClassFileGenerator extends SlangBaseVisitor<Void> {
    private final TraceClassVisitor classWriter;
    private final ClassWriter delegateClassWriter;
    private SlangParser.ProgramContext programContext;
    private String fileName;
    private String className;
    private final FunctionCodeGen mainMethodVisitor;
    private final SymbolTable symbolTable;

    /**
     * A hashmap to store the static variables. The entries are of the form:
     * symbolName: corresponding ISymbol
     */
    private final HashMap<String, ISymbol> staticVariables = new HashMap<>();

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
                if (!symbol.isInitialValueCalculated()) {
                    // Runtime evaluation
                    if (initExpr != null) {
                        IntExprCodeGen intExprCodeGen = new IntExprCodeGen(
                                initExpr,
                                symbolTable,
                                mainMethodVisitor,
                                className,
                                ""
                        );
                        intExprCodeGen.doCodeGen();
                    } else {
                        mainMethodVisitor.getMv().visitLdcInsn(SymbolType.INT.getDefaultValue());
                    }

                    mainMethodVisitor.getMv().visitFieldInsn(
                            Opcodes.PUTSTATIC,
                            className,
                            symbol.getName(),
                            Type.INT_TYPE.getDescriptor()
                    );
                }
                break;

            case BOOL:
                // If we are here, that means initExpr is null and the Symbol is a bool symbol
                // So for boolean field dynamic initialization: we don't do that here
                // (it's done in initializeBooleanField() as it requires a BooleanExprContext)
                break;

            case STRING:
                if (!symbol.isInitialValueCalculated()) {
                    if (initExpr != null) {
                        StringExprCodeGen stringExprCodeGen = new StringExprCodeGen(
                                initExpr,
                                symbolTable,
                                mainMethodVisitor,
                                className,
                                ""
                        );
                        stringExprCodeGen.doCodeGen();
                    } else {
                        mainMethodVisitor.getMv().visitLdcInsn(SymbolType.STRING.getDefaultValue());
                    }

                    // the string should now be on the top of the stack
                    mainMethodVisitor.getMv().visitFieldInsn(
                            Opcodes.PUTSTATIC,
                            className,
                            symbol.getName(),
                            Type.getType(String.class).getDescriptor()
                    );
                }
                break;
        }
    }

    private void initializeBooleanField(ISymbol symbol, SlangParser.BooleanExprContext initExpr) {
        if (symbol.getSymbolType() != SymbolType.BOOL) {
            return;
        }
        if (!symbol.isInitialValueCalculated()) {
            BooleanExprCodeGen booleanExprCodeGen = new BooleanExprCodeGen(
                    initExpr,
                    symbolTable,
                    mainMethodVisitor,
                    className,
                    ""
            );
            booleanExprCodeGen.doCodeGen();

            mainMethodVisitor.getMv().visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    className,
                    symbol.getName(),
                    Type.BOOLEAN_TYPE.getDescriptor()
            );
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
    public Void visitBooleanDeclAssignStmt(SlangParser.BooleanDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = makeFieldFromSymbol(idName);
        if (symbol != null) {
            initializeBooleanField(symbol, ctx.booleanExpr());
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
    public Void visitTypeInferredBooleanDeclAssignStmt(SlangParser.TypeInferredBooleanDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = makeFieldFromSymbol(idName);
        if (symbol != null) {
            initializeBooleanField(symbol, ctx.booleanExpr());
        }
        return null;
    }

    @Override
    public Void visitExprAssign(SlangParser.ExprAssignContext ctx) {
        // look up the identifier on the left to determine the type of the expression
        // because static type check has already ensured compatibility on both sides
        String idName = ctx.IDENTIFIER().getText();

        Pair<ISymbol, Integer> lookupInfo = symbolTable.lookupWithNearestScopeValue(idName);
        if (lookupInfo.getFirst() == null) {
            // lookup failed
            return null;
        }

        Type type = null;
        int storeInstruction = Opcodes.ASTORE;

        // Do codegen of RHS
        switch (lookupInfo.getFirst().getSymbolType()) {
            case INT:
                type = Type.INT_TYPE;
                storeInstruction = Opcodes.ISTORE;
                IntExprCodeGen intCodeGen = new IntExprCodeGen(
                        ctx.expr(), symbolTable, mainMethodVisitor, className, "");
                intCodeGen.doCodeGen();
                break;

            case BOOL:
                // This is the case when either of these occurs:
                // aBoolVar = () -> aFunctionReturningBool.
                // or,
                // aBoolVar = anotherBoolVar.
                type = Type.BOOLEAN_TYPE;
                storeInstruction = Opcodes.ISTORE;
                BooleanExprCodeGen boolCodeGen = new BooleanExprCodeGen(
                        null, symbolTable, mainMethodVisitor, className, "");
                boolCodeGen.doSpecialCodeGen(ctx.expr());
                break;

            case STRING:
                type = Type.getType(String.class);
                StringExprCodeGen stringExprCodeGen = new StringExprCodeGen(
                        ctx.expr(), symbolTable, mainMethodVisitor, className, "");
                stringExprCodeGen.doCodeGen();
                break;

            default:
                err("[Error] Wrong assignment (bad type on LHS).");
        }

        // Store the value generated into the variable
        if (lookupInfo.getSecond() == 0) {
            // we're talking about a global variable
            // (a static field of the class during generation)
            assert type != null;
            mainMethodVisitor.getMv().visitFieldInsn(
                    Opcodes.PUTSTATIC, className, idName, type.getDescriptor()
            );
        } else {
            Integer localVarIndex = mainMethodVisitor.getLocalVarIndex(idName);
            mainMethodVisitor.getMv().visitVarInsn(storeInstruction, localVarIndex);
        }
        return super.visitExprAssign(ctx);
    }

    @Override
    public Void visitBooleanExprAssign(SlangParser.BooleanExprAssignContext ctx) {
        String idName = ctx.IDENTIFIER().getText();

        Pair<ISymbol, Integer> lookupInfo = symbolTable.lookupWithNearestScopeValue(idName);
        if (lookupInfo.getFirst() == null) {
            // lookup failed
            return null;
        }

        // Let's just trust the compile-time type checker here
        Type type = Type.BOOLEAN_TYPE;
        int storeInstruction = Opcodes.ISTORE;

        // Do codegen of RHS
        BooleanExprCodeGen boolCodeGen = new BooleanExprCodeGen(
                ctx.booleanExpr(), symbolTable, mainMethodVisitor, className, "");
        boolCodeGen.doCodeGen();

        // Store the value generated into the variable
        if (lookupInfo.getSecond() == 0) {
            // we're talking about a global variable
            // (a static field of the class during generation)
            mainMethodVisitor.getMv().visitFieldInsn(
                    Opcodes.PUTSTATIC, className, idName, type.getDescriptor()
            );
        } else {
            Integer localVarIndex = mainMethodVisitor.getLocalVarIndex(idName);
            mainMethodVisitor.getMv().visitVarInsn(storeInstruction, localVarIndex);
        }

        return super.visitBooleanExprAssign(ctx);
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
