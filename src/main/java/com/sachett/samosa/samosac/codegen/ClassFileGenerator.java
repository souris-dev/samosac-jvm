package com.sachett.samosa.samosac.codegen;

import com.sachett.samosa.samosac.codegen.compoundstmt.FunctionCodegen;
import com.sachett.samosa.samosac.codegen.expressions.BooleanExprCodegen;
import com.sachett.samosa.samosac.codegen.expressions.IntExprCodegen;
import com.sachett.samosa.samosac.codegen.expressions.StringExprCodegen;
import com.sachett.samosa.samosac.codegen.function.FunctionGenerationContext;
import com.sachett.samosa.samosac.codegen.utils.delegation.CodegenDelegatedMethod;
import com.sachett.samosa.samosac.codegen.utils.delegation.CodegenDelegationManager;
import com.sachett.samosa.samosac.codegen.utils.delegation.CodegenDelegatable;
import com.sachett.samosa.samosac.symbol.*;
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable;

import com.sachett.samosa.parser.SamosaParser;
import kotlin.Pair;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class ClassFileGenerator extends CodegenDelegatable {
    private final TraceClassVisitor classWriter;
    private final ClassWriter delegateClassWriter;
    private final SamosaParser.ProgramContext programContext;
    private String fileName;
    private String className;
    private final ArrayDeque<FunctionGenerationContext> functionGenerationContextStack = new ArrayDeque<>();
    private FunctionGenerationContext currentFunctionGenerationContext;
    private final CodegenCommons delegateCodegenCommons;
    private final SymbolTable symbolTable;

    /**
     * The CodegenDelegationManager helps manage the delegation of the partial code generators.
     */
    private CodegenDelegationManager sharedCodeGenDelegationManager
            = new CodegenDelegationManager(this, null);

    public ClassFileGenerator(
            SamosaParser.ProgramContext programContext,
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
        this.setDelegationManager(sharedCodeGenDelegationManager);

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
        currentFunctionGenerationContext = new FunctionGenerationContext(
                classWriter,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null
        );
        currentFunctionGenerationContext.getMv().visitCode();
        delegateCodegenCommons = new CodegenCommons(this,
                currentFunctionGenerationContext,
                symbolTable,
                className, ""
        );
    }

    public void generateClass() {
        this.visit(this.programContext);
        currentFunctionGenerationContext.getMv().visitInsn(Opcodes.RETURN); // end main function
        currentFunctionGenerationContext.getMv().visitMaxs(0, 0);
        currentFunctionGenerationContext.getMv().visitEnd();
        classWriter.visitEnd();
        CheckClassAdapter.verify(new ClassReader(delegateClassWriter.toByteArray()), true, new PrintWriter(System.out));
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

    private void initializeField(ISymbol symbol, @Nullable SamosaParser.ExprContext initExpr) {
        switch (symbol.getSymbolType()) {
            case INT:
                if (!symbol.isInitialValueCalculated()) {
                    // Runtime evaluation
                    if (initExpr != null) {
                        IntExprCodegen intExprCodegen = new IntExprCodegen(
                                initExpr,
                                symbolTable,
                                currentFunctionGenerationContext,
                                className,
                                ""
                        );
                        intExprCodegen.doCodegen();
                    } else {
                        currentFunctionGenerationContext.getMv().visitLdcInsn(SymbolType.INT.getDefaultValue());
                    }

                    currentFunctionGenerationContext.getMv().visitFieldInsn(
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
                        StringExprCodegen stringExprCodegen = new StringExprCodegen(
                                initExpr,
                                symbolTable,
                                currentFunctionGenerationContext,
                                className,
                                ""
                        );
                        stringExprCodegen.doCodegen();
                    } else {
                        currentFunctionGenerationContext.getMv().visitLdcInsn(SymbolType.STRING.getDefaultValue());
                    }

                    // the string should now be on the top of the stack
                    currentFunctionGenerationContext.getMv().visitFieldInsn(
                            Opcodes.PUTSTATIC,
                            className,
                            symbol.getName(),
                            Type.getType(String.class).getDescriptor()
                    );
                }
                break;
        }
    }

    private void initializeBooleanField(ISymbol symbol, SamosaParser.BooleanExprContext initExpr) {
        if (symbol.getSymbolType() != SymbolType.BOOL) {
            return;
        }
        if (!symbol.isInitialValueCalculated()) {
            BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                    initExpr,
                    symbolTable,
                    currentFunctionGenerationContext,
                    className,
                    ""
            );
            booleanExprCodegen.doCodegen();

            currentFunctionGenerationContext.getMv().visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    className,
                    symbol.getName(),
                    Type.BOOLEAN_TYPE.getDescriptor()
            );
        }
    }

    @Override
    public Void visitBlock(SamosaParser.BlockContext ctx) {
        delegateCodegenCommons.visitBlock(ctx);
        return null;
    }

    @Override
    public Void visitDeclStmt(SamosaParser.DeclStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = makeFieldFromSymbol(idName);
        if (symbol != null) {
            initializeField(symbol, null);
        }
        return null;
    }

    @Override
    public Void visitNormalDeclAssignStmt(SamosaParser.NormalDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = makeFieldFromSymbol(idName);
        if (symbol != null) {
            initializeField(symbol, ctx.expr());
        }
        return null;
    }

    @Override
    public Void visitBooleanDeclAssignStmt(SamosaParser.BooleanDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = makeFieldFromSymbol(idName);
        if (symbol != null) {
            initializeBooleanField(symbol, ctx.booleanExpr());
        }
        return null;
    }

    @Override
    public Void visitTypeInferredDeclAssignStmt(SamosaParser.TypeInferredDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = makeFieldFromSymbol(idName);
        if (symbol != null) {
            initializeField(symbol, ctx.expr());
        }
        return null;
    }

    @Override
    public Void visitTypeInferredBooleanDeclAssignStmt(SamosaParser.TypeInferredBooleanDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = makeFieldFromSymbol(idName);
        if (symbol != null) {
            initializeBooleanField(symbol, ctx.booleanExpr());
        }
        return null;
    }

    @Override
    public Void visitExprAssign(SamosaParser.ExprAssignContext ctx) {
        delegateCodegenCommons.visitExprAssign(ctx);
        return null;
    }

    @Override
    public Void visitBooleanExprAssign(SamosaParser.BooleanExprAssignContext ctx) {
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
        BooleanExprCodegen boolCodegen = new BooleanExprCodegen(
                ctx.booleanExpr(), symbolTable, currentFunctionGenerationContext, className, "");
        boolCodegen.doCodegen();

        // Store the value generated into the variable
        if (lookupInfo.getSecond() == 0) {
            // we're talking about a global variable
            // (a static field of the class during generation)
            currentFunctionGenerationContext.getMv().visitFieldInsn(
                    Opcodes.PUTSTATIC, className, idName, type.getDescriptor()
            );
        } else {
            Integer localVarIndex = currentFunctionGenerationContext.getLocalVarIndex(idName);
            currentFunctionGenerationContext.getMv().visitVarInsn(storeInstruction, localVarIndex);
        }

        return super.visitBooleanExprAssign(ctx);
    }

    @Override
    public Void visitFunctionCallNoArgs(SamosaParser.FunctionCallNoArgsContext ctx) {
        return delegateCodegenCommons.visitFunctionCallNoArgs(ctx);
    }

    @Override
    public Void visitFunctionCallWithArgs(SamosaParser.FunctionCallWithArgsContext ctx) {
        return delegateCodegenCommons.visitFunctionCallWithArgs(ctx);
    }

    private void setCurrentFunctionCodegen(FunctionGenerationContext functionGenerationContext) {
        // save current functionGenerationContext to a stack
        functionGenerationContextStack.push(currentFunctionGenerationContext);
        currentFunctionGenerationContext = functionGenerationContext;

        // Update functionCodeGens of delegates
        delegateCodegenCommons.setFunctionCodegen(currentFunctionGenerationContext); // TODO: refactor this redundancy
    }

    private void restoreLastFunctionCodegen() {
        currentFunctionGenerationContext = functionGenerationContextStack.pop();

        // Update functionCodeGens of delegates
        delegateCodegenCommons.setFunctionCodegen(currentFunctionGenerationContext);
    }

    private FunctionCodegen makeMethod(String funcIdName) {
        var funcSymbol = symbolTable.lookup(funcIdName);

        if (funcSymbol == null) {
            return null;
        }

        if (!(funcSymbol instanceof FunctionSymbol)) {
            return null;
        }

        var functionSymbol = (FunctionSymbol) funcSymbol;

        String funcDescriptor = FunctionGenerationContext.generateDescriptor(functionSymbol);
        FunctionGenerationContext functionGenerationContext = new FunctionGenerationContext(
                classWriter,
                Opcodes.ACC_STATIC + Opcodes.ACC_PUBLIC,
                functionSymbol.getName(),
                funcDescriptor,
                null, null
        );

        setCurrentFunctionCodegen(functionGenerationContext);

        return new FunctionCodegen(
                this, functionGenerationContext,
                delegateCodegenCommons, symbolTable, functionSymbol, className, ""
        );
    }

    @Override
    public Void visitImplicitRetTypeFuncDef(SamosaParser.ImplicitRetTypeFuncDefContext ctx) {
        String funcIdName = ctx.IDENTIFIER().getText();
        FunctionCodegen functionCodegen = makeMethod(funcIdName);
        if (functionCodegen == null) return null;

        this.startDelegatingTo(functionCodegen);
        functionCodegen.generateImplicitRetTypeFuncDef(ctx);
        functionCodegen.endFunctionVisit();
        this.finishDelegating();

        // restore previous functionGenerationContext
        restoreLastFunctionCodegen();
        return null;
    }

    @Override
    public Void visitExplicitRetTypeFuncDef(SamosaParser.ExplicitRetTypeFuncDefContext ctx) {
        String funcIdName = ctx.IDENTIFIER().getText();
        FunctionCodegen functionCodegen = makeMethod(funcIdName);
        if (functionCodegen == null) return null;

        this.startDelegatingTo(functionCodegen);
        functionCodegen.generateExplicitRetTypeFuncDef(ctx);
        functionCodegen.endFunctionVisit();
        this.finishDelegating();

        // restore previous functionGenerationContext
        restoreLastFunctionCodegen();
        return null;
    }

    @Override
    public Void visitIfStmt(SamosaParser.IfStmtContext ctx) {
        return delegateCodegenCommons.visitIfStmt(ctx);
    }

    @Override
    public Void visitWhileStmt(SamosaParser.WhileStmtContext ctx) {
        return delegateCodegenCommons.visitWhileStmt(ctx);
    }

    @Override
    public Void visitBreakControlStmt(SamosaParser.BreakControlStmtContext ctx) {
        return delegateCodegenCommons.visitBreakControlStmt(ctx);
    }

    @Override
    public Void visitContinueControlStmt(SamosaParser.ContinueControlStmtContext ctx) {
        return delegateCodegenCommons.visitContinueControlStmt(ctx);
    }

    @Override
    public Void visitUncertainCompoundStmtSingle(SamosaParser.UncertainCompoundStmtSingleContext ctx) {
        return delegateCodegenCommons.visitUncertainCompoundStmtSingle(ctx);
    }

    @Override
    public Void visitUncertainCompoundStmtMultiple(SamosaParser.UncertainCompoundStmtMultipleContext ctx) {
        return delegateCodegenCommons.visitUncertainCompoundStmtMultiple(ctx);
    }

    @Override
    public Void visitUncertainStatementSingle(SamosaParser.UncertainStatementSingleContext ctx) {
        return delegateCodegenCommons.visitUncertainStatementSingle(ctx);
    }

    @Override
    public Void visitUncertainStatementMultiple(SamosaParser.UncertainStatementMultipleContext ctx) {
        return delegateCodegenCommons.visitUncertainStatementMultiple(ctx);
    }
}
