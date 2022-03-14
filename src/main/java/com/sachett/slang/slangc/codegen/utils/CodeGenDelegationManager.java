package com.sachett.slang.slangc.codegen.utils;

import com.sachett.slang.parser.SlangBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

public class CodeGenDelegationManager extends SlangBaseVisitor<Void> {
    private ICodeGenDelegatable currentCodeGenDelegator;
    private ICodeGenDelegatable currentCodeGenDelegated;

    public CodeGenDelegationManager(ICodeGenDelegatable currentCodeGenDelegator) {
        this.currentCodeGenDelegator = currentCodeGenDelegator;
    }

    public CodeGenDelegationManager(
            ICodeGenDelegatable currentCodeGenDelegator,
            ICodeGenDelegatable currentCodeGenDelegated
    ) {
        this.currentCodeGenDelegator = currentCodeGenDelegator;
        this.currentCodeGenDelegated = currentCodeGenDelegated;
    }

    public void setCurrentDelegator(ICodeGenDelegatable parentDelegator) {
        this.currentCodeGenDelegator = parentDelegator;
    }

    public void setCurrentDelegated(ICodeGenDelegatable childDelegated) {
        this.currentCodeGenDelegated = childDelegated;
    }

    @Override
    public Void visit(ParseTree parseTree) {
        CodeGenDelegatedMethod method = CodeGenMethodMap.getMethodFromClass(parseTree.getClass());
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
        CodeGenDelegatedMethod method = CodeGenMethodMap.getMethodFromClass(parseTree.getClass());
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
