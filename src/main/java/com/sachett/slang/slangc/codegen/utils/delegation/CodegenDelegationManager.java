package com.sachett.slang.slangc.codegen.utils.delegation;

import com.sachett.slang.parser.SlangBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

public class CodegenDelegationManager extends SlangBaseVisitor<Void> {
    private CodegenDelegatable currentCodeGenDelegator;
    private CodegenDelegatable currentCodeGenDelegated;

    public CodegenDelegationManager(CodegenDelegatable currentCodeGenDelegator) {
        this.currentCodeGenDelegator = currentCodeGenDelegator;
    }

    public CodegenDelegationManager(
            CodegenDelegatable currentCodeGenDelegator,
            CodegenDelegatable currentCodeGenDelegated
    ) {
        this.currentCodeGenDelegator = currentCodeGenDelegator;
        this.currentCodeGenDelegated = currentCodeGenDelegated;
    }

    public void setCurrentDelegator(CodegenDelegatable parentDelegator) {
        this.currentCodeGenDelegator = parentDelegator;
    }

    public void setCurrentDelegated(CodegenDelegatable childDelegated) {
        this.currentCodeGenDelegated = childDelegated;
    }

    @Override
    public Void visit(ParseTree parseTree) {
        CodegenDelegatedMethod method = CodegenMethodMap.getMethodFromClass(parseTree.getClass());
        if (method == null) {
            currentCodeGenDelegator.visit(parseTree);
        }

        if (currentCodeGenDelegated == null) {
            if (currentCodeGenDelegator == null) {
                return null;
            }
            else {
                return currentCodeGenDelegator.visit(parseTree);
            }
        }

        if (currentCodeGenDelegated.isMethodDelegated(method)) {
            currentCodeGenDelegated.visit(parseTree);
        }
        else {
            if (currentCodeGenDelegator == null) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Void visitChildren(RuleNode parseTree) {
        CodegenDelegatedMethod method = CodegenMethodMap.getMethodFromClass(parseTree.getClass());
        if (method == null) {
            currentCodeGenDelegator.visitChildren(parseTree);
        }

        if (currentCodeGenDelegated == null) {
            if (currentCodeGenDelegator == null) {
                return null;
            }
            else {
                return currentCodeGenDelegator.visitChildren(parseTree);
            }
        }

        if (currentCodeGenDelegated.isMethodDelegated(method)) {
            currentCodeGenDelegated.visitChildren(parseTree);
        }
        else {
            if (currentCodeGenDelegator == null) {
                return null;
            }
        }
        return null;
    }
}
