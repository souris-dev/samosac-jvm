package com.sachett.slang.slangc.codegen.compoundstmt;

import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.expressions.BooleanExprCodegen;
import com.sachett.slang.slangc.codegen.function.FunctionGenerationContext;
import com.sachett.slang.slangc.codegen.utils.delegation.CodegenDelegatable;
import com.sachett.slang.slangc.codegen.utils.delegation.CodegenDelegatedMethod;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class WhileStmtCodegen extends CodegenDelegatable implements IControlNodeCodegen {
    private FunctionGenerationContext functionGenerationContext;

    private boolean generatingWhileBlock = false;
    private Label whileLoopStartLabel = null;
    private Label whileLoopExitLabel = null;

    private CodegenDelegatable delegatedParentCodegen;

    public WhileStmtCodegen(
            CodegenDelegatable delegatedParentCodegen,
            FunctionGenerationContext functionGenerationContext,
            SymbolTable symbolTable,
            String className,
            String packageName
    ) {
        super(delegatedParentCodegen.getSharedDelegationManager());

        /**
         * Register the stuff that this generator generates with the shared delegation manager.
         */
        HashSet<CodegenDelegatedMethod> delegatedMethodHashSet = new HashSet<>(List.of(CodegenDelegatedMethod.BLOCK,
                CodegenDelegatedMethod.BREAK,
                CodegenDelegatedMethod.CONTINUE
        ));
        this.registerDelegatedMethods(delegatedMethodHashSet);

        this.functionGenerationContext = functionGenerationContext;
        this.delegatedParentCodegen = delegatedParentCodegen;
        this.className = className;
        this.packageName = packageName;
        this.symbolTable = symbolTable;
    }

    // Delegate methods:
    @Override
    public Void visitBooleanExprAssign(SlangParser.BooleanExprAssignContext ctx) {
        return delegatedParentCodegen.visitBooleanExprAssign(ctx);
    }

    @Override
    public Void visitDeclStmt(SlangParser.DeclStmtContext ctx) {
        return delegatedParentCodegen.visitDeclStmt(ctx);
    }

    @Override
    public Void visitBooleanDeclAssignStmt(SlangParser.BooleanDeclAssignStmtContext ctx) {
        return delegatedParentCodegen.visitBooleanDeclAssignStmt(ctx);
    }

    @Override
    public Void visitNormalDeclAssignStmt(SlangParser.NormalDeclAssignStmtContext ctx) {
        return delegatedParentCodegen.visitNormalDeclAssignStmt(ctx);
    }

    @Override
    public Void visitTypeInferredDeclAssignStmt(SlangParser.TypeInferredDeclAssignStmtContext ctx) {
        return delegatedParentCodegen.visitTypeInferredDeclAssignStmt(ctx);
    }

    @Override
    public Void visitTypeInferredBooleanDeclAssignStmt(SlangParser.TypeInferredBooleanDeclAssignStmtContext ctx) {
        return delegatedParentCodegen.visitTypeInferredBooleanDeclAssignStmt(ctx);
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

    private String className;
    private String packageName;
    private SymbolTable symbolTable;

    public void setDelegatedParentCodegen(CodegenDelegatable delegatedParentCodegen) {
        this.delegatedParentCodegen = delegatedParentCodegen;
    }

    public void setFunctionCodegen(FunctionGenerationContext functionGenerationContext) {
        this.functionGenerationContext = functionGenerationContext;
    }

    // Methods handled by this class (not delegated to parent):

    @Override
    public Void visitBreakControlStmt(SlangParser.BreakControlStmtContext ctx) {
        if (generatingWhileBlock && whileLoopExitLabel != null && whileLoopStartLabel != null) {
            functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, whileLoopExitLabel);
        }
        undelegateSelf();
        return null;
    }

    @Override
    public Void visitContinueControlStmt(SlangParser.ContinueControlStmtContext ctx) {
        if (generatingWhileBlock && whileLoopExitLabel != null && whileLoopStartLabel != null) {
            functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, whileLoopStartLabel);
        }
        undelegateSelf();
        return null;
    }

    @Override
    public Void visitExprAssign(SlangParser.ExprAssignContext ctx) {
        return delegatedParentCodegen.visitExprAssign(ctx);
    }

    public void generateWhileStmt(SlangParser.WhileStmtContext ctx) {
        Label loopLabel = new Label();
        Label exitLoopLabel = new Label();
        this.whileLoopStartLabel = loopLabel;
        this.whileLoopExitLabel = exitLoopLabel;

        var currentStackFrame = functionGenerationContext.getCurrentFrameStackInfo();

        functionGenerationContext.getMv().visitLabel(loopLabel);
        functionGenerationContext.getMv().visitFrame(Opcodes.F_NEW,
                currentStackFrame.numLocals, currentStackFrame.locals,
                currentStackFrame.numStack, currentStackFrame.stack
        );

        // check condition
        BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                ctx.booleanExpr(),
                symbolTable,
                functionGenerationContext,
                className,
                packageName
        );
        booleanExprCodegen.doCodegen();

        // if condition is false, exit loop
        currentStackFrame = functionGenerationContext.getCurrentFrameStackInfo();
        functionGenerationContext.getMv().visitJumpInsn(Opcodes.IFEQ, exitLoopLabel);

        this.generatingWhileBlock = true;
        undelegateSelf();
        visit(ctx.block());

        this.generatingWhileBlock = false;
        this.whileLoopStartLabel = null;
        this.whileLoopExitLabel = null;

        // start next iteration
        functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, loopLabel);
        functionGenerationContext.getMv().visitLabel(exitLoopLabel);
        functionGenerationContext.getMv().visitFrame(Opcodes.F_NEW,
                currentStackFrame.numLocals, currentStackFrame.locals,
                currentStackFrame.numStack, currentStackFrame.stack
        );
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        if (Objects.equals(node.getSymbol().getText(), "}")) {
            undelegateSelf();
        }
        return super.visitTerminal(node);
    }
}
