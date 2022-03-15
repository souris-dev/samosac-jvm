package com.sachett.slang.slangc.codegen;

import com.sachett.slang.slangc.codegen.compoundstmt.FunctionGenerator;
import com.sachett.slang.slangc.codegen.expressions.BooleanExprCodegen;
import com.sachett.slang.slangc.codegen.expressions.IntExprCodegen;
import com.sachett.slang.slangc.codegen.expressions.StringExprCodegen;
import com.sachett.slang.slangc.codegen.function.FunctionCodegen;
import com.sachett.slang.slangc.codegen.utils.delegation.CodegenDelegatedMethod;
import com.sachett.slang.slangc.codegen.utils.delegation.CodegenDelegationManager;
import com.sachett.slang.slangc.codegen.utils.delegation.CodegenDelegatable;
import com.sachett.slang.slangc.symbol.*;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;

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
import java.util.*;

public class ClassFileGenerator extends CodegenDelegatable {
    private final TraceClassVisitor classWriter;
    private final ClassWriter delegateClassWriter;
    private final SlangParser.ProgramContext programContext;
    private String fileName;
    private String className;
    private final ArrayDeque<FunctionCodegen> functionCodegenStack = new ArrayDeque<>();
    private FunctionCodegen currentFunctionCodegen;
    private final CodegenCommons delegateCodegenCommons;
    private final SymbolTable symbolTable;

    /**
     * The CodegenDelegationManager helps manage the delegation of the partial code generators.
     */
    private CodegenDelegationManager sharedCodeGenDelegationManager
            = new CodegenDelegationManager(this, null);

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
        super();

        /**
         * Register the stuff that this generator generates with the shared delegation manager.
         */
        HashSet<CodegenDelegatedMethod> delegatedMethodHashSet = new HashSet<>(List.of(CodegenDelegatedMethod.BLOCK,
                CodegenDelegatedMethod.DECL,
                CodegenDelegatedMethod.NORMAL_DECLASSIGN,
                CodegenDelegatedMethod.BOOLEAN_DECLASSIGN,
                CodegenDelegatedMethod.TYPEINF_DECLASSIGN,
                CodegenDelegatedMethod.TYPEINF_BOOLEAN_DECLASSIGN,
                CodegenDelegatedMethod.EXPR_ASSIGN,
                CodegenDelegatedMethod.BOOLEAN_EXPR_ASSIGN,
                CodegenDelegatedMethod.FUNCTIONCALL_NOARGS,
                CodegenDelegatedMethod.FUNCTIONCALL_WITHARGS,
                CodegenDelegatedMethod.IMPLICIT_RET_FUNCDEF,
                CodegenDelegatedMethod.EXPLICIT_RET_FUNCDEF,
                CodegenDelegatedMethod.IF,
                CodegenDelegatedMethod.WHILE,
                CodegenDelegatedMethod.BREAK,
                CodegenDelegatedMethod.CONTINUE));
        this.registerDelegatedMethods(delegatedMethodHashSet);

        /**
         * Initialize the class file generator.
         */

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
        // TODO: Make this COMPUTE_MAXS and compute frames properly in jumps
        // This is being done already, but for some reason the JVM complains EVEN IF the stack frames are consistent.
        // To try it, change COMPUTE_FRAMES to COMPUTE_MAXS and try running the generated class file.
        this.delegateClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        this.classWriter = new TraceClassVisitor(delegateClassWriter, new PrintWriter(System.out));
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, this.className, null, "java/lang/Object", null);

        // Generate a default main function
        currentFunctionCodegen = new FunctionCodegen(
                classWriter,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null
        );
        currentFunctionCodegen.getMv().visitCode();
        delegateCodegenCommons = new CodegenCommons(this,
                currentFunctionCodegen,
                symbolTable,
                className, ""
        );
    }

    public void generateClass() {
        this.visit(this.programContext);
        currentFunctionCodegen.getMv().visitInsn(Opcodes.RETURN);
        currentFunctionCodegen.getMv().visitMaxs(0, 0); // any arguments work, will be recalculated
        currentFunctionCodegen.getMv().visitEnd();
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
                        IntExprCodegen intExprCodeGen = new IntExprCodegen(
                                initExpr,
                                symbolTable,
                                currentFunctionCodegen,
                                className,
                                ""
                        );
                        intExprCodeGen.doCodeGen();
                    } else {
                        currentFunctionCodegen.getMv().visitLdcInsn(SymbolType.INT.getDefaultValue());
                    }

                    currentFunctionCodegen.getMv().visitFieldInsn(
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
                        StringExprCodegen stringExprCodeGen = new StringExprCodegen(
                                initExpr,
                                symbolTable,
                                currentFunctionCodegen,
                                className,
                                ""
                        );
                        stringExprCodeGen.doCodeGen();
                    } else {
                        currentFunctionCodegen.getMv().visitLdcInsn(SymbolType.STRING.getDefaultValue());
                    }

                    // the string should now be on the top of the stack
                    currentFunctionCodegen.getMv().visitFieldInsn(
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
            BooleanExprCodegen booleanExprCodeGen = new BooleanExprCodegen(
                    initExpr,
                    symbolTable,
                    currentFunctionCodegen,
                    className,
                    ""
            );
            booleanExprCodeGen.doCodeGen();

            currentFunctionCodegen.getMv().visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    className,
                    symbol.getName(),
                    Type.BOOLEAN_TYPE.getDescriptor()
            );
        }
    }

    @Override
    public Void visitBlock(SlangParser.BlockContext ctx) {
        delegateCodegenCommons.visitBlock(ctx);
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
        delegateCodegenCommons.visitExprAssign(ctx);
        return null;
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
        BooleanExprCodegen boolCodeGen = new BooleanExprCodegen(
                ctx.booleanExpr(), symbolTable, currentFunctionCodegen, className, "");
        boolCodeGen.doCodeGen();

        // Store the value generated into the variable
        if (lookupInfo.getSecond() == 0) {
            // we're talking about a global variable
            // (a static field of the class during generation)
            currentFunctionCodegen.getMv().visitFieldInsn(
                    Opcodes.PUTSTATIC, className, idName, type.getDescriptor()
            );
        } else {
            Integer localVarIndex = currentFunctionCodegen.getLocalVarIndex(idName);
            currentFunctionCodegen.getMv().visitVarInsn(storeInstruction, localVarIndex);
        }

        return super.visitBooleanExprAssign(ctx);
    }

    @Override
    public Void visitFunctionCallNoArgs(SlangParser.FunctionCallNoArgsContext ctx) {
        return delegateCodegenCommons.visitFunctionCallNoArgs(ctx);
    }

    @Override
    public Void visitFunctionCallWithArgs(SlangParser.FunctionCallWithArgsContext ctx) {
        return delegateCodegenCommons.visitFunctionCallWithArgs(ctx);
    }

    private void setCurrentFunctionCodeGen(FunctionCodegen functionCodeGen) {
        // save current functionCodeGen to a stack
        functionCodegenStack.push(currentFunctionCodegen);
        currentFunctionCodegen = functionCodeGen;

        // Update functionCodeGens of delegates
        delegateCodegenCommons.setFunctionCodeGen(currentFunctionCodegen); // TODO: refactor this redundancy
    }

    private void restoreLastFunctionCodeGen() {
        currentFunctionCodegen = functionCodegenStack.pop();

        // Update functionCodeGens of delegates
        delegateCodegenCommons.setFunctionCodeGen(currentFunctionCodegen);
    }

    private FunctionGenerator makeMethod(String funcIdName) {
        var funcSymbol = symbolTable.lookup(funcIdName);

        if (funcSymbol == null) {
            return null;
        }

        if (!(funcSymbol instanceof FunctionSymbol functionSymbol)) {
            return null;
        }

        String funcDescriptor = FunctionCodegen.generateDescriptor(functionSymbol);
        FunctionCodegen functionCodeGen = new FunctionCodegen(
                classWriter,
                Opcodes.ACC_STATIC + Opcodes.ACC_PUBLIC,
                functionSymbol.getName(),
                funcDescriptor,
                null, null
        );

        setCurrentFunctionCodeGen(functionCodeGen);

        return new FunctionGenerator(
                this, functionCodeGen,
                delegateCodegenCommons, symbolTable, functionSymbol, className, ""
        );
    }

    @Override
    public Void visitImplicitRetTypeFuncDef(SlangParser.ImplicitRetTypeFuncDefContext ctx) {
        String funcIdName = ctx.IDENTIFIER().getText();
        FunctionGenerator functionGenerator = makeMethod(funcIdName);
        if (functionGenerator == null) return null;

        this.startDelegatingTo(functionGenerator);
        functionGenerator.generateImplicitRetTypeFuncDef(ctx);
        this.finishDelegating();

        // restore previous functionCodeGen
        restoreLastFunctionCodeGen();
        return null;
    }

    @Override
    public Void visitExplicitRetTypeFuncDef(SlangParser.ExplicitRetTypeFuncDefContext ctx) {
        String funcIdName = ctx.IDENTIFIER().getText();
        FunctionGenerator functionGenerator = makeMethod(funcIdName);
        if (functionGenerator == null) return null;

        this.startDelegatingTo(functionGenerator);
        functionGenerator.generateExplicitRetTypeFuncDef(ctx);
        this.finishDelegating();

        // restore previous functionCodeGen
        restoreLastFunctionCodeGen();
        return null;
    }

    @Override
    public Void visitIfStmt(SlangParser.IfStmtContext ctx) {
        return delegateCodegenCommons.visitIfStmt(ctx);
    }

    @Override
    public Void visitWhileStmt(SlangParser.WhileStmtContext ctx) {
        return delegateCodegenCommons.visitWhileStmt(ctx);
    }

    @Override
    public Void visitBreakControlStmt(SlangParser.BreakControlStmtContext ctx) {
        return delegateCodegenCommons.visitBreakControlStmt(ctx);
    }

    @Override
    public Void visitContinueControlStmt(SlangParser.ContinueControlStmtContext ctx) {
        return delegateCodegenCommons.visitContinueControlStmt(ctx);
    }
}
