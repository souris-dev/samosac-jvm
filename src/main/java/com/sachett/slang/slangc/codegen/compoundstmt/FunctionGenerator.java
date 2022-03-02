package com.sachett.slang.slangc.codegen.compoundstmt;

import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.CodeGenerator;
import com.sachett.slang.slangc.codegen.CommonCodeGen;
import com.sachett.slang.slangc.codegen.expressions.BooleanExprCodeGen;
import com.sachett.slang.slangc.codegen.expressions.IntExprCodeGen;
import com.sachett.slang.slangc.codegen.expressions.StringExprCodeGen;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.staticchecker.ExpressionTypeDetector;
import com.sachett.slang.slangc.symbol.ISymbol;
import com.sachett.slang.slangc.symbol.SymbolType;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class FunctionGenerator extends CodeGenerator {
    private SymbolTable symbolTable;
    private FunctionCodeGen functionCodeGen;
    private CodeGenerator delegatedParentCodegen;
    private CommonCodeGen commonCodeGen;
    private String className;
    private String packageName;

    public FunctionGenerator(
            CodeGenerator delegatedParentCodegen,
            FunctionCodeGen functionCodeGen,
            CommonCodeGen commonCodeGen,
            SymbolTable symbolTable,
            String className,
            String packageName
    ) {
        this.functionCodeGen = functionCodeGen;
        this.delegatedParentCodegen = delegatedParentCodegen;
        this.commonCodeGen = commonCodeGen;
        this.symbolTable = symbolTable;
        this.className = className;
        this.packageName = packageName;
    }

    @Override
    public Void visitReturnStmtNoExpr(SlangParser.ReturnStmtNoExprContext ctx) {
        functionCodeGen.getMv().visitInsn(Opcodes.RETURN);
        return null;
    }

    @Override
    public Void visitReturnStmtWithExpr(SlangParser.ReturnStmtWithExprContext ctx) {
        ExpressionTypeDetector typeDetector = new ExpressionTypeDetector(symbolTable);
        Pair<Boolean, SymbolType> typeInfo = typeDetector.getType(ctx.expr());
        if (typeInfo.getFirst()) {
            switch (typeInfo.getSecond()) {
                case INT -> {
                    IntExprCodeGen intExprCodeGen = new IntExprCodeGen(
                            ctx.expr(),
                            symbolTable, functionCodeGen,
                            className, packageName
                    );
                    intExprCodeGen.doCodeGen();
                    functionCodeGen.getMv().visitInsn(Opcodes.IRETURN);
                }
                case STRING -> {
                    StringExprCodeGen strExprCodeGen = new StringExprCodeGen(
                            ctx.expr(),
                            symbolTable, functionCodeGen,
                            className, packageName
                    );
                    strExprCodeGen.doCodeGen();
                    functionCodeGen.getMv().visitInsn(Opcodes.ARETURN);
                }
                case BOOL -> {
                    // This again, is either of these scenarios:
                    // return boolVar.
                    // or,
                    // return () -> boolValReturnFunc.
                    BooleanExprCodeGen booleanExprCodeGen = new BooleanExprCodeGen(
                            null,
                            symbolTable, functionCodeGen,
                            className, packageName
                    );
                    booleanExprCodeGen.doSpecialCodeGen(ctx.expr());
                    functionCodeGen.getMv().visitInsn(Opcodes.IRETURN);
                }
            }
        }
        return null;
    }

    @Override
    public Void visitReturnStmtWithBooleanExpr(SlangParser.ReturnStmtWithBooleanExprContext ctx) {
        BooleanExprCodeGen booleanExprCodeGen = new BooleanExprCodeGen(
                ctx.booleanExpr(),
                symbolTable, functionCodeGen,
                className, packageName
        );
        booleanExprCodeGen.doCodeGen();
        functionCodeGen.getMv().visitInsn(Opcodes.IRETURN);

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
                functionCodeGen.newLocal(symbolAugmentedName, Type.INT_TYPE);
                functionCodeGen.getMv().visitLdcInsn(symbolTypeDefaultValue);
                functionCodeGen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodeGen.getLocalVarIndex(symbolAugmentedName));
            }
            case STRING -> {
                functionCodeGen.newLocal(symbol.getAugmentedName(), Type.getType(String.class));
                functionCodeGen.getMv().visitLdcInsn(symbolTypeDefaultValue);
                functionCodeGen.getMv().visitVarInsn(Opcodes.ASTORE, functionCodeGen.getLocalVarIndex(symbolAugmentedName));
            }
            case BOOL -> {
                functionCodeGen.newLocal(symbol.getAugmentedName(), Type.BOOLEAN_TYPE);
                symbolTypeDefaultValue = Boolean.TRUE.equals(symbolType.getDefaultValue()) ? 1 : 0;
                functionCodeGen.getMv().visitLdcInsn(symbolTypeDefaultValue);
                functionCodeGen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodeGen.getLocalVarIndex(symbolAugmentedName));
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

        functionCodeGen.newLocal(symbol.getAugmentedName(), Type.BOOLEAN_TYPE);
        BooleanExprCodeGen booleanExprCodeGen = new BooleanExprCodeGen(
                ctx.booleanExpr(),
                symbolTable, functionCodeGen,
                className, packageName
        );
        booleanExprCodeGen.doCodeGen();
        functionCodeGen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodeGen.getLocalVarIndex(symbol.getAugmentedName()));

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
                functionCodeGen.newLocal(symbolAugmentedName, Type.INT_TYPE);
                IntExprCodeGen intExprCodeGen = new IntExprCodeGen(ctx.expr(), symbolTable, functionCodeGen, className, packageName);
                intExprCodeGen.doCodeGen();
                functionCodeGen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodeGen.getLocalVarIndex(symbolAugmentedName));
            }
            case STRING -> {
                functionCodeGen.newLocal(symbolAugmentedName, Type.getType(String.class));
                StringExprCodeGen strExprCodeGen = new StringExprCodeGen(ctx.expr(), symbolTable, functionCodeGen, className, packageName);
                strExprCodeGen.doCodeGen();
                functionCodeGen.getMv().visitVarInsn(Opcodes.ASTORE, functionCodeGen.getLocalVarIndex(symbolAugmentedName));
            }
            case BOOL -> {
                // This again, is either of these scenarios:
                // bro, boolVar = boolVar2.
                // or,
                // bro, boolVar = () -> boolValReturnFunc.
                functionCodeGen.newLocal(symbolAugmentedName, Type.BOOLEAN_TYPE);
                BooleanExprCodeGen booleanExprCodeGen = new BooleanExprCodeGen(null, symbolTable, functionCodeGen, className, packageName);
                booleanExprCodeGen.doSpecialCodeGen(ctx.expr());
                functionCodeGen.getMv().visitVarInsn(Opcodes.ISTORE, functionCodeGen.getLocalVarIndex(symbolAugmentedName));
            }
        }

        return null;
    }

    @Override
    public Void visitTypeInferredDeclAssignStmt(SlangParser.TypeInferredDeclAssignStmtContext ctx) {
        return super.visitTypeInferredDeclAssignStmt(ctx);
    }

    @Override
    public Void visitTypeInferredBooleanDeclAssignStmt(SlangParser.TypeInferredBooleanDeclAssignStmtContext ctx) {
        return super.visitTypeInferredBooleanDeclAssignStmt(ctx);
    }

    public Void generateImplicitRetTypeFuncDef(SlangParser.ImplicitRetTypeFuncDefContext ctx) {
        return super.visitImplicitRetTypeFuncDef(ctx);
    }

    public Void generateExplicitRetTypeFuncDef(SlangParser.ExplicitRetTypeFuncDefContext ctx) {
        return super.visitExplicitRetTypeFuncDef(ctx);
    }
}
