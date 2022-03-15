package com.sachett.slang.slangc.codegen.compoundstmt;

import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.CodeGenerator;
import com.sachett.slang.slangc.codegen.CodegenCommons;
import com.sachett.slang.slangc.codegen.expressions.BooleanExprCodegen;
import com.sachett.slang.slangc.codegen.expressions.IntExprCodegen;
import com.sachett.slang.slangc.codegen.expressions.StringExprCodegen;
import com.sachett.slang.slangc.codegen.function.FunctionCodegen;
import com.sachett.slang.slangc.codegen.utils.delegation.CodegenDelegatedMethod;
import com.sachett.slang.slangc.codegen.utils.delegation.CodegenDelegatable;
import com.sachett.slang.slangc.staticchecker.ExpressionTypeDetector;
import com.sachett.slang.slangc.symbol.FunctionSymbol;
import com.sachett.slang.slangc.symbol.ISymbol;
import com.sachett.slang.slangc.symbol.SymbolType;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.List;

public class FunctionGenerator extends CodegenDelegatable {
    private SymbolTable symbolTable;
    private FunctionCodegen functionCodegen;

    @Override
    public Void visitBreakControlStmt(SlangParser.BreakControlStmtContext ctx) {
        return delegatedParentCodegen.visitBreakControlStmt(ctx);
    }

    @Override
    public Void visitContinueControlStmt(SlangParser.ContinueControlStmtContext ctx) {
        return delegatedParentCodegen.visitContinueControlStmt(ctx);
    }

    @Override
    public Void visitBlock(SlangParser.BlockContext ctx) {
        return delegatedParentCodegen.visitBlock(ctx);
    }

    @Override
    public Void visitIfStmt(SlangParser.IfStmtContext ctx) {
        return delegatedParentCodegen.visitIfStmt(ctx);
    }

    @Override
    public Void visitWhileStmt(SlangParser.WhileStmtContext ctx) {
        return delegatedParentCodegen.visitWhileStmt(ctx);
    }

    @Override
    public Void visitFunctionCallNoArgs(SlangParser.FunctionCallNoArgsContext ctx) {
        return delegatedParentCodegen.visitFunctionCallNoArgs(ctx);
    }

    @Override
    public Void visitFunctionCallWithArgs(SlangParser.FunctionCallWithArgsContext ctx) {
        return delegatedParentCodegen.visitFunctionCallWithArgs(ctx);
    }

    private CodegenDelegatable delegatedParentCodegen;
    private CodegenCommons codegenCommons;
    private String className;
    private String packageName;
    private FunctionSymbol functionSymbol;

    public FunctionGenerator(
            CodegenDelegatable delegatedParentCodegen,
            FunctionCodegen functionCodegen,
            CodegenCommons codegenCommons,
            SymbolTable symbolTable,
            FunctionSymbol functionSymbol,
            String className,
            String packageName
    ) {
        super(delegatedParentCodegen.getSharedDelegationManager());

        /**
         * Register with the manager the stuff that this partial generator generates.
         */
        HashSet<CodegenDelegatedMethod> delegatedMethodHashSet = new HashSet<>(List.of(CodegenDelegatedMethod.BLOCK,
                CodegenDelegatedMethod.RETURN_NOEXPR,
                CodegenDelegatedMethod.RETURN_WITHEXPR,
                CodegenDelegatedMethod.RETURN_BOOL,
                CodegenDelegatedMethod.DECL,
                CodegenDelegatedMethod.BOOLEAN_DECLASSIGN,
                CodegenDelegatedMethod.NORMAL_DECLASSIGN,
                CodegenDelegatedMethod.TYPEINF_DECLASSIGN,
                CodegenDelegatedMethod.TYPEINF_BOOLEAN_DECLASSIGN
        ));
        this.registerDelegatedMethods(delegatedMethodHashSet);

        this.functionCodegen = functionCodegen;
        this.delegatedParentCodegen = delegatedParentCodegen;
        this.codegenCommons = codegenCommons;
        this.symbolTable = symbolTable;
        this.className = className;
        this.functionSymbol = functionSymbol;
        this.packageName = packageName;
    }

    @Override
    public Void visitReturnStmtNoExpr(SlangParser.ReturnStmtNoExprContext ctx) {
        functionCodegen.getMv().visitInsn(Opcodes.RETURN);
        return null;
    }

    @Override
    public Void visitReturnStmtWithExpr(SlangParser.ReturnStmtWithExprContext ctx) {
        ExpressionTypeDetector typeDetector = new ExpressionTypeDetector(symbolTable);
        Pair<Boolean, SymbolType> typeInfo = typeDetector.getType(ctx.expr());
        if (typeInfo.getFirst()) {
            switch (typeInfo.getSecond()) {
                case INT -> {
                    IntExprCodegen intExprCodegen = new IntExprCodegen(
                            ctx.expr(),
                            symbolTable, functionCodegen,
                            className, packageName
                    );
                    intExprCodegen.doCodegen();
                    functionCodegen.getMv().visitInsn(Opcodes.IRETURN);
                }
                case STRING -> {
                    StringExprCodegen strExprCodegen = new StringExprCodegen(
                            ctx.expr(),
                            symbolTable, functionCodegen,
                            className, packageName
                    );
                    strExprCodegen.doCodegen();
                    functionCodegen.getMv().visitInsn(Opcodes.ARETURN);
                }
                case BOOL -> {
                    // This again, is either of these scenarios:
                    // return boolVar.
                    // or,
                    // return () -> boolValReturnFunc.
                    BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                            null,
                            symbolTable, functionCodegen,
                            className, packageName
                    );
                    booleanExprCodegen.doSpecialCodegen(ctx.expr());
                    functionCodegen.getMv().visitInsn(Opcodes.IRETURN);
                }
            }
        }
        return null;
    }

    @Override
    public Void visitReturnStmtWithBooleanExpr(SlangParser.ReturnStmtWithBooleanExprContext ctx) {
        BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                ctx.booleanExpr(),
                symbolTable, functionCodegen,
                className, packageName
        );
        booleanExprCodegen.doCodegen();
        functionCodegen.getMv().visitInsn(Opcodes.IRETURN);

        return null;
    }

    @Override
    public Void visitDeclStmt(SlangParser.DeclStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = symbolTable.lookupInCurrentScopeOnly(idName);

        if (symbol == null) {
            return null;
        }

        SymbolType symbolType = symbol.getSymbolType();
        String symbolAugmentedName = symbol.getAugmentedName();
        Object symbolTypeDefaultValue = symbolType.getDefaultValue();
        switch (symbolType) {
            case INT -> {
                functionCodegen.newLocal(symbolAugmentedName, Type.INT_TYPE);
                functionCodegen.getMv().visitLdcInsn(symbolTypeDefaultValue);
                functionCodegen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodegen.getLocalVarIndex(symbolAugmentedName));
            }
            case STRING -> {
                functionCodegen.newLocal(symbol.getAugmentedName(), Type.getType(String.class));
                functionCodegen.getMv().visitLdcInsn(symbolTypeDefaultValue);
                functionCodegen.getMv().visitVarInsn(Opcodes.ASTORE, functionCodegen.getLocalVarIndex(symbolAugmentedName));
            }
            case BOOL -> {
                functionCodegen.newLocal(symbol.getAugmentedName(), Type.BOOLEAN_TYPE);
                symbolTypeDefaultValue = Boolean.TRUE.equals(symbolType.getDefaultValue()) ? 1 : 0;
                functionCodegen.getMv().visitLdcInsn(symbolTypeDefaultValue);
                functionCodegen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodegen.getLocalVarIndex(symbolAugmentedName));
            }
        }

        return null;
    }

    @Override
    public Void visitBooleanDeclAssignStmt(SlangParser.BooleanDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = symbolTable.lookupInCurrentScopeOnly(idName);

        if (symbol == null) {
            return null;
        }

        functionCodegen.newLocal(symbol.getAugmentedName(), Type.BOOLEAN_TYPE);
        BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                ctx.booleanExpr(),
                symbolTable, functionCodegen,
                className, packageName
        );
        booleanExprCodegen.doCodegen();
        functionCodegen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodegen.getLocalVarIndex(symbol.getAugmentedName()));

        return null;
    }

    @Override
    public Void visitNormalDeclAssignStmt(SlangParser.NormalDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = symbolTable.lookupInCurrentScopeOnly(idName);

        if (symbol == null) {
            return null;
        }

        SymbolType symbolType = symbol.getSymbolType();
        String symbolAugmentedName = symbol.getAugmentedName();
        switch (symbolType) {
            case INT -> {
                functionCodegen.newLocal(symbolAugmentedName, Type.INT_TYPE);
                IntExprCodegen intExprCodegen = new IntExprCodegen(ctx.expr(), symbolTable, functionCodegen, className, packageName);
                intExprCodegen.doCodegen();
                functionCodegen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodegen.getLocalVarIndex(symbolAugmentedName));
            }
            case STRING -> {
                functionCodegen.newLocal(symbolAugmentedName, Type.getType(String.class));
                StringExprCodegen strExprCodegen = new StringExprCodegen(ctx.expr(), symbolTable, functionCodegen, className, packageName);
                strExprCodegen.doCodegen();
                functionCodegen.getMv().visitVarInsn(Opcodes.ASTORE, functionCodegen.getLocalVarIndex(symbolAugmentedName));
            }
            case BOOL -> {
                // This again, is either of these scenarios:
                // bro, boolVar = boolVar2.
                // or,
                // bro, boolVar = () -> boolValReturnFunc.
                functionCodegen.newLocal(symbolAugmentedName, Type.BOOLEAN_TYPE);
                BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(null, symbolTable, functionCodegen, className, packageName);
                booleanExprCodegen.doSpecialCodegen(ctx.expr());
                functionCodegen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodegen.getLocalVarIndex(symbolAugmentedName));
            }
        }

        return null;
    }

    @Override
    public Void visitTypeInferredDeclAssignStmt(SlangParser.TypeInferredDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = symbolTable.lookupInCurrentScopeOnly(idName);

        if (symbol == null) {
            return null;
        }

        ExpressionTypeDetector typeDetector = new ExpressionTypeDetector(symbolTable);
        Pair<Boolean, SymbolType> symbolTypeInfo = typeDetector.getType(ctx.expr());
        String symbolAugmentedName = symbol.getAugmentedName();

        if (!symbolTypeInfo.getFirst()) {
            return null;
        }

        switch (symbolTypeInfo.getSecond()) {
            case INT -> {
                functionCodegen.newLocal(symbolAugmentedName, Type.INT_TYPE);
                IntExprCodegen intExprCodegen = new IntExprCodegen(ctx.expr(), symbolTable, functionCodegen, className, packageName);
                intExprCodegen.doCodegen();
                functionCodegen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodegen.getLocalVarIndex(symbolAugmentedName));
            }
            case STRING -> {
                functionCodegen.newLocal(symbolAugmentedName, Type.getType(String.class));
                StringExprCodegen strExprCodegen = new StringExprCodegen(ctx.expr(), symbolTable, functionCodegen, className, packageName);
                strExprCodegen.doCodegen();
                functionCodegen.getMv().visitVarInsn(Opcodes.ASTORE, functionCodegen.getLocalVarIndex(symbolAugmentedName));
            }
            case BOOL -> {
                // This again, is either of these scenarios:
                // bro, boolVar = boolVar2.
                // or,
                // bro, boolVar = () -> boolValReturnFunc.
                functionCodegen.newLocal(symbolAugmentedName, Type.BOOLEAN_TYPE);
                BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(null, symbolTable, functionCodegen, className, packageName);
                booleanExprCodegen.doSpecialCodegen(ctx.expr());
                functionCodegen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodegen.getLocalVarIndex(symbolAugmentedName));
            }
        }

        return null;
    }

    @Override
    public Void visitTypeInferredBooleanDeclAssignStmt(SlangParser.TypeInferredBooleanDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = symbolTable.lookupInCurrentScopeOnly(idName);

        if (symbol == null) {
            return null;
        }

        functionCodegen.newLocal(symbol.getAugmentedName(), Type.BOOLEAN_TYPE);
        BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                ctx.booleanExpr(),
                symbolTable, functionCodegen,
                className, packageName
        );
        booleanExprCodegen.doCodegen();
        functionCodegen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodegen.getLocalVarIndex(symbol.getAugmentedName()));

        return null;
    }

    public void registerArguments() {
        // Add arguments into the local variables map
        int localVarSlot = 0;

        for (ISymbol symbol : functionSymbol.getParamList()) {
            functionCodegen.registerLocal(symbol.getAugmentedName(), localVarSlot);

            // TODO: When long/doubles/arrays are added, localVarSlot will have to be be incremented by more than one for them
            localVarSlot++;
        }
    }

    public void generateImplicitRetTypeFuncDef(SlangParser.ImplicitRetTypeFuncDefContext ctx) {
        registerArguments();
        super.visitImplicitRetTypeFuncDef(ctx);
    }

    public void generateExplicitRetTypeFuncDef(SlangParser.ExplicitRetTypeFuncDefContext ctx) {
        registerArguments();
        super.visitExplicitRetTypeFuncDef(ctx);
    }
}
