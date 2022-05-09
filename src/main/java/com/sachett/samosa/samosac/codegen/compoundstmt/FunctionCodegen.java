package com.sachett.samosa.samosac.codegen.compoundstmt;

import com.sachett.samosa.parser.SamosaParser;
import com.sachett.samosa.samosac.codegen.CodegenCommons;
import com.sachett.samosa.samosac.codegen.expressions.BooleanExprCodegen;
import com.sachett.samosa.samosac.codegen.expressions.IntExprCodegen;
import com.sachett.samosa.samosac.codegen.expressions.StringExprCodegen;
import com.sachett.samosa.samosac.codegen.function.FunctionGenerationContext;
import com.sachett.samosa.samosac.codegen.utils.delegation.CodegenDelegatedMethod;
import com.sachett.samosa.samosac.codegen.utils.delegation.CodegenDelegatable;
import com.sachett.samosa.samosac.staticchecker.ExpressionTypeDetector;
import com.sachett.samosa.samosac.symbol.FunctionSymbol;
import com.sachett.samosa.samosac.symbol.ISymbol;
import com.sachett.samosa.samosac.symbol.SymbolType;
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.List;

public class FunctionCodegen extends CodegenDelegatable {
    private SymbolTable symbolTable;
    private FunctionGenerationContext functionGenerationContext;

    private CodegenDelegatable delegatedParentCodegen;
    private CodegenCommons codegenCommons;
    private String className;
    private String packageName;
    private FunctionSymbol functionSymbol;

    public FunctionCodegen(
            CodegenDelegatable delegatedParentCodegen,
            FunctionGenerationContext functionGenerationContext,
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
                CodegenDelegatedMethod.TYPEINF_BOOLEAN_DECLASSIGN,
                CodegenDelegatedMethod.EXPR_ASSIGN,
                CodegenDelegatedMethod.BOOLEAN_EXPR_ASSIGN,
                CodegenDelegatedMethod.BREAK,
                CodegenDelegatedMethod.CONTINUE,
                CodegenDelegatedMethod.BLOCK,
                CodegenDelegatedMethod.IF,
                CodegenDelegatedMethod.WHILE,
                CodegenDelegatedMethod.IMPLICIT_RET_FUNCDEF,
                CodegenDelegatedMethod.EXPLICIT_RET_FUNCDEF,
                CodegenDelegatedMethod.FUNCTIONCALL_NOARGS,
                CodegenDelegatedMethod.FUNCTIONCALL_WITHARGS
        ));
        this.registerDelegatedMethods(delegatedMethodHashSet);

        this.functionGenerationContext = functionGenerationContext;
        this.delegatedParentCodegen = delegatedParentCodegen;
        this.codegenCommons = codegenCommons;
        this.symbolTable = symbolTable;
        this.className = className;
        this.functionSymbol = functionSymbol;
        this.packageName = packageName;
    }

    @Override
    public Void visitBreakControlStmt(SamosaParser.BreakControlStmtContext ctx) {
        return delegatedParentCodegen.visitBreakControlStmt(ctx);
    }

    @Override
    public Void visitContinueControlStmt(SamosaParser.ContinueControlStmtContext ctx) {
        return delegatedParentCodegen.visitContinueControlStmt(ctx);
    }

    @Override
    public Void visitBlock(SamosaParser.BlockContext ctx) {
        return delegatedParentCodegen.visitBlock(ctx);
    }

    @Override
    public Void visitExprAssign(SamosaParser.ExprAssignContext ctx) {
        return delegatedParentCodegen.visitExprAssign(ctx);
    }

    @Override
    public Void visitBooleanExprAssign(SamosaParser.BooleanExprAssignContext ctx) {
        return delegatedParentCodegen.visitBooleanExprAssign(ctx);
    }

    @Override
    public Void visitUncertainCompoundStmtSingle(SamosaParser.UncertainCompoundStmtSingleContext ctx) {
        var savedParentCodegen = codegenCommons.getParentCodegen();
        codegenCommons.setParentCodegen(this);
        var _placeholder = codegenCommons.visitUncertainCompoundStmtSingle(ctx);
        codegenCommons.setParentCodegen(savedParentCodegen);
        return _placeholder;
    }

    @Override
    public Void visitUncertainCompoundStmtMultiple(SamosaParser.UncertainCompoundStmtMultipleContext ctx) {
        var savedParentCodegen = codegenCommons.getParentCodegen();
        codegenCommons.setParentCodegen(this);
        var _placeholder = codegenCommons.visitUncertainCompoundStmtMultiple(ctx);
        codegenCommons.setParentCodegen(savedParentCodegen);
        return _placeholder;
    }

    @Override
    public Void visitUncertainStatementSingle(SamosaParser.UncertainStatementSingleContext ctx) {
        var savedParentCodegen = codegenCommons.getParentCodegen();
        codegenCommons.setParentCodegen(this);
        var _placeholder = codegenCommons.visitUncertainStatementSingle(ctx);
        codegenCommons.setParentCodegen(savedParentCodegen);
        return _placeholder;
    }

    @Override
    public Void visitUncertainStatementMultiple(SamosaParser.UncertainStatementMultipleContext ctx) {
        var savedParentCodegen = codegenCommons.getParentCodegen();
        codegenCommons.setParentCodegen(this);
        var _placeholder = codegenCommons.visitUncertainStatementMultiple(ctx);
        codegenCommons.setParentCodegen(savedParentCodegen);
        return _placeholder;
    }

    @Override
    public Void visitWhileStmt(SamosaParser.WhileStmtContext ctx) {
        CodegenDelegatable previousParentCodegen = codegenCommons.getParentCodegen();
        codegenCommons.setParentCodegen(this);
        codegenCommons.visitWhileStmt(ctx);
        codegenCommons.setParentCodegen(previousParentCodegen);
        return null;
    }

    @Override
    public Void visitIfStmt(SamosaParser.IfStmtContext ctx) {
        CodegenDelegatable previousParentCodegen = codegenCommons.getParentCodegen();
        codegenCommons.setParentCodegen(this);
        codegenCommons.visitIfStmt(ctx);
        codegenCommons.setParentCodegen(previousParentCodegen);
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SamosaParser.FunctionCallNoArgsContext ctx) {
        return delegatedParentCodegen.visitFunctionCallNoArgs(ctx);
    }

    @Override
    public Void visitFunctionCallWithArgs(SamosaParser.FunctionCallWithArgsContext ctx) {
        return delegatedParentCodegen.visitFunctionCallWithArgs(ctx);
    }

    @Override
    public Void visitReturnStmtNoExpr(SamosaParser.ReturnStmtNoExprContext ctx) {
        functionGenerationContext.getMv().visitInsn(Opcodes.RETURN);
        return null;
    }

    @Override
    public Void visitReturnStmtWithExpr(SamosaParser.ReturnStmtWithExprContext ctx) {
        ExpressionTypeDetector typeDetector = new ExpressionTypeDetector(symbolTable);
        Pair<Boolean, SymbolType> typeInfo = typeDetector.getType(ctx.expr());
        if (typeInfo.getFirst()) {
            switch (typeInfo.getSecond()) {
                case INT:
                    IntExprCodegen intExprCodegen = new IntExprCodegen(
                            ctx.expr(),
                            symbolTable, functionGenerationContext,
                            className, packageName
                    );
                    intExprCodegen.doCodegen();
                    functionGenerationContext.getMv().visitInsn(Opcodes.IRETURN);
                    break;
                case STRING:
                    StringExprCodegen strExprCodegen = new StringExprCodegen(
                            ctx.expr(),
                            symbolTable, functionGenerationContext,
                            className, packageName
                    );
                    strExprCodegen.doCodegen();
                    functionGenerationContext.getMv().visitInsn(Opcodes.ARETURN);
                    break;
                case BOOL:
                    // This again, is either of these scenarios:
                    // return boolVar.
                    // or,
                    // return () -> boolValReturnFunc.
                    BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                            null,
                            symbolTable, functionGenerationContext,
                            className, packageName
                    );
                    booleanExprCodegen.doSpecialCodegen(ctx.expr());
                    functionGenerationContext.getMv().visitInsn(Opcodes.IRETURN);
                    break;
            }
        }

        undelegateSelf();
        return null;
    }

    @Override
    public Void visitReturnStmtWithBooleanExpr(SamosaParser.ReturnStmtWithBooleanExprContext ctx) {
        BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                ctx.booleanExpr(),
                symbolTable, functionGenerationContext,
                className, packageName
        );
        booleanExprCodegen.doCodegen();
        functionGenerationContext.getMv().visitInsn(Opcodes.IRETURN);

        undelegateSelf();
        return null;
    }

    @Override
    public Void visitDeclStmt(SamosaParser.DeclStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = symbolTable.lookupInCurrentScopeOnly(idName);

        if (symbol == null) {
            return null;
        }

        SymbolType symbolType = symbol.getSymbolType();
        String symbolAugmentedName = symbol.getAugmentedName();
        Object symbolTypeDefaultValue = symbolType.getDefaultValue();
        switch (symbolType) {
            case INT:
                functionGenerationContext.newLocal(symbolAugmentedName, Type.INT_TYPE);
                functionGenerationContext.getMv().visitLdcInsn(symbolTypeDefaultValue);
                functionGenerationContext.getMv().visitVarInsn(Opcodes.ISTORE, functionGenerationContext.getLocalVarIndex(symbolAugmentedName));
                break;
            case STRING:
                functionGenerationContext.newLocal(symbol.getAugmentedName(), Type.getType(String.class));
                functionGenerationContext.getMv().visitLdcInsn(symbolTypeDefaultValue);
                functionGenerationContext.getMv().visitVarInsn(Opcodes.ASTORE, functionGenerationContext.getLocalVarIndex(symbolAugmentedName));
                break;
            case BOOL:
                functionGenerationContext.newLocal(symbol.getAugmentedName(), Type.BOOLEAN_TYPE);
                symbolTypeDefaultValue = Boolean.TRUE.equals(symbolType.getDefaultValue()) ? 1 : 0;
                functionGenerationContext.getMv().visitLdcInsn(symbolTypeDefaultValue);
                functionGenerationContext.getMv().visitVarInsn(Opcodes.ISTORE, functionGenerationContext.getLocalVarIndex(symbolAugmentedName));
                break;
        }

        undelegateSelf();
        return null;
    }

    @Override
    public Void visitBooleanDeclAssignStmt(SamosaParser.BooleanDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = symbolTable.lookupInCurrentScopeOnly(idName);

        if (symbol == null) {
            return null;
        }

        functionGenerationContext.newLocal(symbol.getAugmentedName(), Type.BOOLEAN_TYPE);
        BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                ctx.booleanExpr(),
                symbolTable, functionGenerationContext,
                className, packageName
        );
        booleanExprCodegen.doCodegen();
        functionGenerationContext.getMv().visitVarInsn(Opcodes.ISTORE, functionGenerationContext.getLocalVarIndex(symbol.getAugmentedName()));

        undelegateSelf();
        return null;
    }

    @Override
    public Void visitNormalDeclAssignStmt(SamosaParser.NormalDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = symbolTable.lookupInCurrentScopeOnly(idName);

        if (symbol == null) {
            return null;
        }

        SymbolType symbolType = symbol.getSymbolType();
        String symbolAugmentedName = symbol.getAugmentedName();
        switch (symbolType) {
            case INT:
                functionGenerationContext.newLocal(symbolAugmentedName, Type.INT_TYPE);
                IntExprCodegen intExprCodegen = new IntExprCodegen(ctx.expr(), symbolTable, functionGenerationContext, className, packageName);
                intExprCodegen.doCodegen();
                functionGenerationContext.getMv().visitVarInsn(Opcodes.ISTORE, functionGenerationContext.getLocalVarIndex(symbolAugmentedName));
                break;
            case STRING:
                functionGenerationContext.newLocal(symbolAugmentedName, Type.getType(String.class));
                StringExprCodegen strExprCodegen = new StringExprCodegen(ctx.expr(), symbolTable, functionGenerationContext, className, packageName);
                strExprCodegen.doCodegen();
                functionGenerationContext.getMv().visitVarInsn(Opcodes.ASTORE, functionGenerationContext.getLocalVarIndex(symbolAugmentedName));
                break;
            case BOOL:
                // This again, is either of these scenarios:
                // bro, boolVar = boolVar2.
                // or,
                // bro, boolVar = () -> boolValReturnFunc.
                functionGenerationContext.newLocal(symbolAugmentedName, Type.BOOLEAN_TYPE);
                BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(null, symbolTable, functionGenerationContext, className, packageName);
                booleanExprCodegen.doSpecialCodegen(ctx.expr());
                functionGenerationContext.getMv().visitVarInsn(Opcodes.ISTORE, functionGenerationContext.getLocalVarIndex(symbolAugmentedName));
                break;
        }

        undelegateSelf();
        return null;
    }

    @Override
    public Void visitTypeInferredDeclAssignStmt(SamosaParser.TypeInferredDeclAssignStmtContext ctx) {
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
            case INT:
                functionGenerationContext.newLocal(symbolAugmentedName, Type.INT_TYPE);
                IntExprCodegen intExprCodegen = new IntExprCodegen(ctx.expr(), symbolTable, functionGenerationContext, className, packageName);
                intExprCodegen.doCodegen();
                functionGenerationContext.getMv().visitVarInsn(Opcodes.ISTORE, functionGenerationContext.getLocalVarIndex(symbolAugmentedName));
                break;
            case STRING:
                functionGenerationContext.newLocal(symbolAugmentedName, Type.getType(String.class));
                StringExprCodegen strExprCodegen = new StringExprCodegen(ctx.expr(), symbolTable, functionGenerationContext, className, packageName);
                strExprCodegen.doCodegen();
                functionGenerationContext.getMv().visitVarInsn(Opcodes.ASTORE, functionGenerationContext.getLocalVarIndex(symbolAugmentedName));
                break;
            case BOOL:
                // This again, is either of these scenarios:
                // bro, boolVar = boolVar2.
                // or,
                // bro, boolVar = () -> boolValReturnFunc.
                functionGenerationContext.newLocal(symbolAugmentedName, Type.BOOLEAN_TYPE);
                BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(null, symbolTable, functionGenerationContext, className, packageName);
                booleanExprCodegen.doSpecialCodegen(ctx.expr());
                functionGenerationContext.getMv().visitVarInsn(Opcodes.ISTORE, functionGenerationContext.getLocalVarIndex(symbolAugmentedName));
                break;
        }

        undelegateSelf();
        return null;
    }

    @Override
    public Void visitTypeInferredBooleanDeclAssignStmt(SamosaParser.TypeInferredBooleanDeclAssignStmtContext ctx) {
        String idName = ctx.IDENTIFIER().getSymbol().getText();
        ISymbol symbol = symbolTable.lookupInCurrentScopeOnly(idName);

        if (symbol == null) {
            return null;
        }

        functionGenerationContext.newLocal(symbol.getAugmentedName(), Type.BOOLEAN_TYPE);
        BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                ctx.booleanExpr(),
                symbolTable, functionGenerationContext,
                className, packageName
        );
        booleanExprCodegen.doCodegen();
        functionGenerationContext.getMv().visitVarInsn(Opcodes.ISTORE, functionGenerationContext.getLocalVarIndex(symbol.getAugmentedName()));

        undelegateSelf();
        return null;
    }

    public void registerArguments() {
        // Add arguments into the local variables map
        int localVarSlot = 0;

        for (ISymbol symbol : functionSymbol.getParamList()) {
            functionGenerationContext.registerLocal(symbol.getAugmentedName(), localVarSlot);

            // TODO: When long/doubles/arrays are added, localVarSlot will have to be be incremented by more than one for them
            localVarSlot++;
        }
    }

    @Override
    public Void visitExplicitRetTypeFuncDef(SamosaParser.ExplicitRetTypeFuncDefContext ctx) {
        return super.visitExplicitRetTypeFuncDef(ctx);
    }

    @Override
    public Void visitImplicitRetTypeFuncDef(SamosaParser.ImplicitRetTypeFuncDefContext ctx) {
        return super.visitImplicitRetTypeFuncDef(ctx);
    }

    public void generateImplicitRetTypeFuncDef(SamosaParser.ImplicitRetTypeFuncDefContext ctx) {
        registerArguments();
        functionGenerationContext.setNeedsNoExprReturn(true);
        visitChildren(ctx);
    }

    public void generateExplicitRetTypeFuncDef(SamosaParser.ExplicitRetTypeFuncDefContext ctx) {
        registerArguments();
        functionGenerationContext.setNeedsNoExprReturn(functionSymbol.getReturnType() == SymbolType.VOID);
        visitChildren(ctx);
    }

    public void endFunctionVisit() {
        if (functionGenerationContext.getNeedsNoExprReturn()) {
            // needs a no-expr RETURN instruction
            functionGenerationContext.getMv().visitInsn(Opcodes.RETURN);
        }

        functionGenerationContext.getMv().visitMaxs(0, 0);
        functionGenerationContext.getMv().visitEnd();
    }
}
