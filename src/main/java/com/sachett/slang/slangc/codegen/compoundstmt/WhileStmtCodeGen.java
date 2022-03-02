package com.sachett.slang.slangc.codegen.compoundstmt;

import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.CodeGenerator;
import com.sachett.slang.slangc.codegen.expressions.BooleanExprCodeGen;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class WhileStmtCodeGen extends CodeGenerator {
    private FunctionCodeGen functionCodeGen;

    private boolean generatingWhileBlock = false;
    private Label whileLoopStartLabel = null;
    private Label whileLoopExitLabel = null;

    private CodeGenerator delegatedParentCodeGen;
    // Delegate methods:
    @Override
    public Void visitBooleanExprAssign(SlangParser.BooleanExprAssignContext ctx) {
        return delegatedParentCodeGen.visitBooleanExprAssign(ctx);
    }

    @Override
    public Void visitDeclStmt(SlangParser.DeclStmtContext ctx) {
        return delegatedParentCodeGen.visitDeclStmt(ctx);
    }

    @Override
    public Void visitBooleanDeclAssignStmt(SlangParser.BooleanDeclAssignStmtContext ctx) {
        return delegatedParentCodeGen.visitBooleanDeclAssignStmt(ctx);
    }

    @Override
    public Void visitNormalDeclAssignStmt(SlangParser.NormalDeclAssignStmtContext ctx) {
        return delegatedParentCodeGen.visitNormalDeclAssignStmt(ctx);
    }

    @Override
    public Void visitTypeInferredDeclAssignStmt(SlangParser.TypeInferredDeclAssignStmtContext ctx) {
        return delegatedParentCodeGen.visitTypeInferredDeclAssignStmt(ctx);
    }

    @Override
    public Void visitTypeInferredBooleanDeclAssignStmt(SlangParser.TypeInferredBooleanDeclAssignStmtContext ctx) {
        return delegatedParentCodeGen.visitTypeInferredBooleanDeclAssignStmt(ctx);
    }

    @Override
    public Void visitBlock(SlangParser.BlockContext ctx) {
        return delegatedParentCodeGen.visitBlock(ctx);
    }

    @Override
    public Void visitIfStmt(SlangParser.IfStmtContext ctx) {
        return delegatedParentCodeGen.visitIfStmt(ctx);
    }

    @Override
    public Void visitWhileStmt(SlangParser.WhileStmtContext ctx) {
        return delegatedParentCodeGen.visitWhileStmt(ctx);
    }

    private String className;
    private String packageName;
    private SymbolTable symbolTable;

    public void setDelegatedParentCodeGen(CodeGenerator delegatedParentCodeGen) {
        this.delegatedParentCodeGen = delegatedParentCodeGen;
    }

    public void setFunctionCodeGen(FunctionCodeGen functionCodeGen) {
        this.functionCodeGen = functionCodeGen;
    }

    public WhileStmtCodeGen(
            CodeGenerator delegatedParentCodeGen,
            FunctionCodeGen functionCodeGen,
            SymbolTable symbolTable,
            String className,
            String packageName
    ) {
        this.functionCodeGen = functionCodeGen;
        this.delegatedParentCodeGen = delegatedParentCodeGen;
        this.className = className;
        this.packageName = packageName;
        this.symbolTable = symbolTable;
    }

    @Override
    public Void visitBreakControlStmt(SlangParser.BreakControlStmtContext ctx) {
        if (generatingWhileBlock && whileLoopExitLabel != null && whileLoopStartLabel != null) {
            functionCodeGen.getMv().visitJumpInsn(Opcodes.GOTO, whileLoopExitLabel);
        }
        return null;
    }

    @Override
    public Void visitContinueControlStmt(SlangParser.ContinueControlStmtContext ctx) {
        if (generatingWhileBlock && whileLoopExitLabel != null && whileLoopStartLabel != null) {
            functionCodeGen.getMv().visitJumpInsn(Opcodes.GOTO, whileLoopStartLabel);
        }
        return null;
    }

    public void generateWhileStmt(SlangParser.WhileStmtContext ctx) {
        Label loopLabel = new Label();
        Label exitLoopLabel = new Label();
        this.whileLoopStartLabel = loopLabel;
        this.whileLoopExitLabel = exitLoopLabel;

        var currentStackFrame = functionCodeGen.getCurrentFrameStackInfo();

        functionCodeGen.getMv().visitLabel(loopLabel);
        functionCodeGen.getMv().visitFrame(Opcodes.F_NEW,
                currentStackFrame.numLocals, currentStackFrame.locals,
                currentStackFrame.numStack, currentStackFrame.stack
        );

        // check condition
        BooleanExprCodeGen booleanExprCodeGen = new BooleanExprCodeGen(
                ctx.booleanExpr(),
                symbolTable,
                functionCodeGen,
                className,
                packageName
        );
        booleanExprCodeGen.doCodeGen();

        // if condition is false, exit loop
        currentStackFrame = functionCodeGen.getCurrentFrameStackInfo();
        functionCodeGen.getMv().visitJumpInsn(Opcodes.IFEQ, exitLoopLabel);

        this.generatingWhileBlock = true;

        visit(ctx.block());

        this.generatingWhileBlock = false;
        this.whileLoopStartLabel = null;
        this.whileLoopExitLabel = null;

        // start next iteration
        functionCodeGen.getMv().visitJumpInsn(Opcodes.GOTO, loopLabel);
        functionCodeGen.getMv().visitLabel(exitLoopLabel);
        functionCodeGen.getMv().visitFrame(Opcodes.F_NEW,
                currentStackFrame.numLocals, currentStackFrame.locals,
                currentStackFrame.numStack, currentStackFrame.stack
        );
    }
}
