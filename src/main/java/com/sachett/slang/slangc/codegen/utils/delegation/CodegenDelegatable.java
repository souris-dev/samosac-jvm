package com.sachett.slang.slangc.codegen.utils.delegation;

import com.sachett.slang.slangc.codegen.CodeGenerator;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.HashSet;

public abstract class CodegenDelegatable extends CodeGenerator {
    HashSet<CodegenDelegatedMethod> delegatedMethods = new HashSet<>();
    CodegenDelegationManager codeGenDelegationManager;

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

    protected void startDelegatingTo(CodegenDelegatable delegatable) {
        codeGenDelegationManager.setCurrentDelegated(delegatable);
        codeGenDelegationManager.setCurrentDelegator(this);
    }

    protected void finishDelegating() {
        codeGenDelegationManager.setCurrentDelegated(null);
        codeGenDelegationManager.setCurrentDelegator(this);
    }

    protected void registerDelegatedMethod(CodegenDelegatedMethod method) {
        this.delegatedMethods.add(method);
    }

    protected void registerDelegatedMethods(HashSet<CodegenDelegatedMethod> methods) {
        this.delegatedMethods.addAll(methods);
    }

    public boolean isMethodDelegated(CodegenDelegatedMethod method) {
        return delegatedMethods.contains(method);
    }

    @Override
    public Void visit(ParseTree parseTree) {
        return codeGenDelegationManager.visit(parseTree);
    }

    @Override
    public Void visitChildren(RuleNode node) {
        return codeGenDelegationManager.visitChildren(node);
    }
}
