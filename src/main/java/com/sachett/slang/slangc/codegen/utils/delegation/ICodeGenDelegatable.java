package com.sachett.slang.slangc.codegen.utils.delegation;

import com.sachett.slang.slangc.codegen.CodeGenerator;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.HashSet;

public abstract class ICodeGenDelegatable extends CodeGenerator {
    HashSet<CodeGenDelegatedMethod> delegatedMethods = new HashSet<>();
    CodeGenDelegationManager codeGenDelegationManager;

    public ICodeGenDelegatable(
            HashSet<CodeGenDelegatedMethod> delegatedMethods,
            CodeGenDelegationManager codeGenDelegationManager
    ) {
        this.codeGenDelegationManager = codeGenDelegationManager;
        this.delegatedMethods = delegatedMethods;
    }

    public ICodeGenDelegatable(
            CodeGenDelegationManager codeGenDelegationManager
    ) {
        this.codeGenDelegationManager = codeGenDelegationManager;
    }

    public ICodeGenDelegatable() {
        this.codeGenDelegationManager = new CodeGenDelegationManager(this);
    }

    public void setDelegationManager(CodeGenDelegationManager manager) {
        this.codeGenDelegationManager = manager;
    }

    public CodeGenDelegationManager getSharedDelegationManager() {
        return codeGenDelegationManager;
    }

    protected void startDelegatingTo(ICodeGenDelegatable delegatable) {
        codeGenDelegationManager.setCurrentDelegated(delegatable);
        codeGenDelegationManager.setCurrentDelegator(this);
    }

    protected void finishDelegating() {
        codeGenDelegationManager.setCurrentDelegated(null);
        codeGenDelegationManager.setCurrentDelegator(this);
    }

    protected void registerDelegatedMethod(CodeGenDelegatedMethod method) {
        this.delegatedMethods.add(method);
    }

    protected void registerDelegatedMethods(HashSet<CodeGenDelegatedMethod> methods) {
        this.delegatedMethods.addAll(methods);
    }

    public boolean isMethodDelegated(CodeGenDelegatedMethod method) {
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
