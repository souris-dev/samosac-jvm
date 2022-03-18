package com.sachett.slang.slangc.codegen.utils.delegation;

import com.sachett.slang.slangc.codegen.CodeGenerator;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.HashSet;

public abstract class CodegenDelegatable extends CodeGenerator {
    HashSet<CodegenDelegatedMethod> delegatedMethods = new HashSet<>();
    CodegenDelegationManager codeGenDelegationManager;
    private boolean beingDelegated = false;
    private boolean wasBeingDelegated = false;

    public CodegenDelegatable(
            HashSet<CodegenDelegatedMethod> delegatedMethods,
            CodegenDelegationManager codeGenDelegationManager
    ) {
        this.codeGenDelegationManager = codeGenDelegationManager;
        this.delegatedMethods = delegatedMethods;
    }

    public CodegenDelegatable(
            CodegenDelegationManager codeGenDelegationManager
    ) {
        this.codeGenDelegationManager = codeGenDelegationManager;
    }

    public CodegenDelegatable() {
        this.codeGenDelegationManager = new CodegenDelegationManager(this);
    }

    public void setDelegationManager(CodegenDelegationManager manager) {
        this.codeGenDelegationManager = manager;
    }

    public CodegenDelegationManager getSharedDelegationManager() {
        return codeGenDelegationManager;
    }

    // Needs to be public so that delegated CodegenCommons can access the parent's method of startDelegatingTo
    public void startDelegatingTo(CodegenDelegatable delegatable) {
        wasBeingDelegated = isBeingDelegated();
        setBeingDelegated(false);
        delegatable.setBeingDelegated(true);
        codeGenDelegationManager.setCurrentDelegated(delegatable);
        codeGenDelegationManager.setCurrentDelegator(this);
    }

    public void finishDelegating() {
        setBeingDelegated(wasBeingDelegated);
        codeGenDelegationManager.setCurrentDelegated(null);
        codeGenDelegationManager.setCurrentDelegator(this);
    }

    public void setBeingDelegated(boolean beingDelegated) {
        this.beingDelegated = beingDelegated;
    }

    public void undelegateSelf() {
        wasBeingDelegated = beingDelegated;
        setBeingDelegated(false);
    }

    public boolean isBeingDelegated() {
        return beingDelegated;
    }

    protected void registerDelegatedMethods(HashSet<CodegenDelegatedMethod> methods) {
        this.delegatedMethods.addAll(methods);
    }

    public boolean isMethodDelegated(CodegenDelegatedMethod method) {
        return delegatedMethods.contains(method);
    }

    @Override
    public Void visit(ParseTree parseTree) {
        System.out.println("Visiting ParseTree \t(type) " + parseTree.getClass().toString() + " \t\t(through) " + this.toString());
        if (isBeingDelegated()) {
            return super.visit(parseTree);
        }
        else {
            return codeGenDelegationManager.visit(parseTree);
        }
    }

    @Override
    public Void visitChildren(RuleNode node) {
        System.out.println("Visiting RuleNode \t(type) " + node.getClass().toString() + " \t\t(through) " + this.toString());
        if (isBeingDelegated()) {
            return super.visitChildren(node);
        }
        else {
            return codeGenDelegationManager.visitChildren(node);
        }
    }
}
